package com.mixfa.ailibrary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.ReadBooksHint;
import com.mixfa.ailibrary.model.suggestion.SuggestedBook;
import com.mixfa.ailibrary.model.suggestion.SuggsetionHint;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.SuggestionService;
import com.mixfa.ailibrary.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Slf4j
@Service
public class SuggestionServiceImpl implements SuggestionService {
    private final UserDataService userDataService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ListOperations<String, BookRecord> listOps;
    private final SearchEngine.ForBooks bookSearchService;

    private static final String BOOK_RECORDS_KEY = "all-books";

    private static final String RESPONSE_JSON_SCHEMA = JsonSchemaGenerator.generateForType(SuggestedBook[].class);
    private static final UserMessage SEARCH_PROMPT;

    static {
        var rootPromptBuilder = new StringBuilder();

        rootPromptBuilder.append("Suggest 3 books to read, based on user`s read books and available books, to list availiable books, use search function\n");
        rootPromptBuilder.append("In response provide: bookId, title and reason(why user should read book)\n");
        rootPromptBuilder.append("You must use ID from book description (from search function), not 1,2,3");
        rootPromptBuilder.append("You must not wrap json in ```json tag");
        rootPromptBuilder.append("You must ALWAYS respond with Json Array, even if single book");
        rootPromptBuilder.append("You are not allowed to respond with question or anything except what you were asked for");
        rootPromptBuilder.append(RESPONSE_JSON_SCHEMA);

        SEARCH_PROMPT = new UserMessage(rootPromptBuilder.toString());
    }

    private static final String JSONIZE_MESSAGE;

    static {
        var jsonizeMessageBuilder = new StringBuilder();
        jsonizeMessageBuilder.append("Write provided list of books as json array, jscon scheme:\n");
        jsonizeMessageBuilder.append("Required fields:\n");
        for (Field field : SuggestedBook.class.getDeclaredFields()) {
            var fieldName = field.getName();
            var type = field.getType().getSimpleName();

            jsonizeMessageBuilder.append(fieldName).append(": ").append(type).append("\n");
        }

        JSONIZE_MESSAGE = jsonizeMessageBuilder.toString();
    }

    public record SearchFunctionArgs(String query, int page) {
    }

    public record BookRecord(
            String id,
            String description) {
        public static BookRecord create(Book book) {
            var id = book.id().toHexString();
            var title = book.titleString(Utils.DEFAULT_LOCALE);

            var sb = new StringBuilder();

            sb.append("Book description").append('\n');
            sb.append("ID = ").append(id).append('\n');
            sb.append("title = ").append(title).append('\n');

            var desc = book.localizedDescription().getOrDefault(Utils.DEFAULT_LOCALE, null);
            if (desc != null)
                sb.append("Description = \n").append(desc).append('\n');

            sb.append("genres = ");
            for (Genre genre : book.genres())
                sb.append(genre.name()).append(", ");
            sb.append("\nAuthors = ");
            for (String author : book.authors())
                sb.append(author).append(", ");
            sb.append('\n');
            return new BookRecord(id, sb.toString());
        }
    }

    public SuggestionServiceImpl(
            UserDataService userDataService,
            ChatModel chatModel,
            RedisTemplate<String, BookRecord> redisTemplate,
            ObjectMapper objectMapper,
            SearchEngine.ForBooks bookSearchService) {
        this.userDataService = userDataService;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.listOps = redisTemplate.opsForList();
        this.bookSearchService = bookSearchService;
    }

    public String prepareBooksContext(SearchOption searchOptions) {
        var sb = new StringBuilder().append("Books present in library:\n");

        if (searchOptions.isEmpty()) {
            long size = Objects.requireNonNull(listOps.size(BOOK_RECORDS_KEY));
            if (size == 0) {

                Utils.iteratePages(bookSearchService::findAll, books -> {
                    var bookRecords = books.getContent().stream().map(BookRecord::create).toArray(BookRecord[]::new);
                    listOps.leftPushAll(BOOK_RECORDS_KEY, bookRecords);

                    for (BookRecord bookRecord : bookRecords) {
                        sb.append(bookRecord.description);
                    }

                    return true;
                });

            } else {
                var pgSize = 30L;
                var pages = Math.ceilDiv(size, pgSize);
                for (int page = 0; page < pages; page++) {
                    var booksRecords = listOps.range(BOOK_RECORDS_KEY, page * pgSize, (page + 1) * pgSize);
                    for (BookRecord booksRecord : Objects.requireNonNull(booksRecords))
                        sb.append(booksRecord.description);
                }
            }
        } else {
            Function<Pageable, Page<Book>> fetchFunc = (Pageable pageable) -> bookSearchService.find(searchOptions,
                    pageable);

            Utils.iteratePages(fetchFunc, books -> {
                var bookRecords = books.getContent().stream().map(BookRecord::create).toArray(BookRecord[]::new);

                for (BookRecord bookRecord : bookRecords) {
                    sb.append(bookRecord.description);
                }

                return true;
            });
        }
        return sb.toString();
    }

    @Override
    public SuggestedBook[] getSuggestions() {
        return getSuggestions(SearchOption.empty());
    }

    @Override
    public SuggestedBook[] getSuggestions(SearchOption searchOptions) {
        var readBooks = userDataService.readBooks().get();
        SuggsetionHint hint = SuggsetionHint.empty();
        if (readBooks.length != 0)
            hint = new ReadBooksHint(readBooks);

        return getSuggestions(SearchOption.empty(), hint);
    }

    @Override
    public SuggestedBook[] getSuggestions(SuggsetionHint suggsetionHint) {
        return getSuggestions(SearchOption.empty(), suggsetionHint);
    }

    public static String makeBookDescription(Book book) {
        var id = book.id().toHexString();
        var title = book.titleString(Utils.DEFAULT_LOCALE);

        var sb = new StringBuilder();

        sb.append("Book description").append('\n');
        sb.append("ID = ").append(id).append('\n');
        sb.append("title = ").append(title).append('\n');

        var desc = book.localizedDescription().getOrDefault(Utils.DEFAULT_LOCALE, null);
        if (desc != null)
            sb.append("Description = \n").append(desc).append('\n');

        sb.append("genres = ");
        for (Genre genre : book.genres())
            sb.append(genre.name()).append(", ");
        sb.append("\nAuthors = ");
        for (String author : book.authors())
            sb.append(author).append(", ");
        sb.append("\n\n");
        return sb.toString();
    }

    private String makeBooksContext(final SearchOption searchOption, final SearchFunctionArgs args) {
        var pageRequest = PageRequest.of(args.page, 15);

        var query = args.query;
        Page<Book> books = Page.empty();
        if (StringUtils.isNotBlank(query)) {
            var compsedSearchOptions = SearchOption.composition(SearchOption.Books.byTitle(query), searchOption);
            books = bookSearchService.find(compsedSearchOptions, pageRequest);
        }
        if (books.isEmpty())
            books = bookSearchService.find(searchOption, pageRequest);

        return fmt("Page: {0}\nTotal pages: {1}\n", args.page, books.getTotalPages()) +
                books.getContent()
                        .stream()
                        .map(SuggestionServiceImpl::makeBookDescription)
                        .collect(Collectors.joining("\n"));
    }

    @Override
    public SuggestedBook[] getSuggestions(SearchOption searchOptions, SuggsetionHint suggsetionHint) {
        var userContext = new UserMessage(suggsetionHint.makeHint());

        var searchTool = FunctionToolCallback.builder("search", (SearchFunctionArgs args) -> makeBooksContext(searchOptions, args))
                .inputType(SearchFunctionArgs.class)
                .description("Search for available books page by page. Params: String query(can be empty), int page(starts from 0)")
                .build();

        var searchPromptOptions = OpenAiChatOptions.builder()
                .toolCallbacks(searchTool)
                .build();

        var searchResult = chatModel.call(new Prompt(List.of(SEARCH_PROMPT, userContext), searchPromptOptions));
        log.info("LLM respond:");
        System.out.println(searchResult);

        var textToParse = searchResult.getResult().getOutput().getText();

        final var jsonPrefix = "```json";
        if (textToParse.startsWith(jsonPrefix))
            textToParse = textToParse.substring(jsonPrefix.length());
        final var jsonPostfix = "```";
        if (textToParse.endsWith(jsonPostfix))
            textToParse = textToParse.substring(0, textToParse.length() - jsonPostfix.length());

        try {
            var output = textToParse;

            var rootTree = objectMapper.readTree(output);

            if (rootTree.isObject()) {
                var tree = Utils.findJsonNode(rootTree, JsonNode::isArray);

                if (tree != null)
                    return new SuggestedBook[]{objectMapper.treeToValue(tree, SuggestedBook.class)};
            }

            return objectMapper.treeToValue(rootTree, SuggestedBook[].class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
