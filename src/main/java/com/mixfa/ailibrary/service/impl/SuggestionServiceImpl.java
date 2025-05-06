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
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class SuggestionServiceImpl implements SuggestionService {
    private final UserDataService userDataService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ValueOperations<String, String> valueOps;
    private final SearchEngine.ForBooks bookSearchService;

    private static final String RESPONSE_JSON_SCHEMA = JsonSchemaGenerator.generateForType(SuggestedBook[].class);
    private static final UserMessage SEARCH_PROMPT = new UserMessage(
            String.join(
                    "\n",
                    "Suggest 3 books to read, based on user`s read books and available books, to list availiable books, use search function",
                    "In response provide: bookId, title and reason(why user should read book)",
                    "You must use ID from book description (from search function), not 1,2,3",
                    "You must not wrap json in ```json tag",
                    "You must ALWAYS respond with Json Array, even if single book",
                    "You are not allowed to respond with question or anything except what you were asked for",
                    RESPONSE_JSON_SCHEMA
            )
    );

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

    private final Retry retry = Retry.of("suggestionService",
            RetryConfig.<SuggestedBook[]>custom()
                    .maxAttempts(3)
                    .failAfterMaxAttempts(true)
                    .waitDuration(Duration.ofMillis(250))
                    .retryOnResult(ArrayUtils::isEmpty)
                    .build());

    private final Function<Prompt, SuggestedBook[]> getAndParseFunc = Retry.decorateFunction(retry, this::getAndParse);

    public record SearchFunctionArgs(String query, int page) {
    }

    public SuggestionServiceImpl(
            UserDataService userDataService,
            ChatModel chatModel,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            SearchEngine.ForBooks bookSearchService) {
        this.userDataService = userDataService;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.valueOps = redisTemplate.opsForValue();
        this.bookSearchService = bookSearchService;
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

    private static String makeBookDescriptionKey(Book book) {
        return makeBookDescriptionKey(book.id().toHexString());
    }

    public static String makeBookDescriptionKey(String id) {
        return id + ":book-desc";
    }

    private String makeBooksContext(final SearchOption searchOption, final SearchFunctionArgs args) {
        var pageRequest = PageRequest.of(args.page, 15);

        var query = args.query;
        Page<Book> booksPage = Page.empty();
        if (StringUtils.isNotBlank(query)) {
            var compsedSearchOptions = SearchOption.composition(SearchOption.Books.byTitle(query), searchOption);
            booksPage = bookSearchService.find(compsedSearchOptions, pageRequest);
        }
        if (booksPage.isEmpty())
            booksPage = bookSearchService.find(searchOption, pageRequest);


        var booksResponseBuilder = new StringBuilder();
        booksResponseBuilder
                .append("Page: ").append(args.page)
                .append("\nTotal pages: ").append(booksPage.getTotalPages())
                .append('\n');

        var books = booksPage.getContent();
        var descriptions = valueOps.multiGet(books.stream().map(SuggestionServiceImpl::makeBookDescriptionKey).toList());

        if (books.size() != descriptions.size())
            throw new RuntimeException("Book description count mismatch (valueOps multiGet)");

        var toSetValues = new HashMap<String, String>();
        for (int i = 0; i < books.size(); i++) {
            var book = books.get(i);
            var description = descriptions.get(i);

            if (description == null) {
                description = makeBookDescription(book);
                toSetValues.put(makeBookDescriptionKey(book), description);
            }

            booksResponseBuilder.append(description);
        }

        if (!toSetValues.isEmpty())
            valueOps.multiSet(toSetValues);

        return booksResponseBuilder.toString();
    }

    private SuggestedBook[] getAndParse(Prompt prompt) {
        var searchResult = chatModel.call(prompt);
        log.info("LLM respond:");
        System.out.println(searchResult);

        var textToParse = searchResult.getResult().getOutput().getText();

        final var jsonPrefix = "```json";
        if (textToParse.startsWith(jsonPrefix))
            textToParse = textToParse.substring(jsonPrefix.length());
        final var jsonPostfix = "```";
        if (textToParse.endsWith(jsonPostfix))
            textToParse = textToParse.substring(0, textToParse.length() - jsonPostfix.length());

        var output = textToParse;

        try {
            var rootTree = objectMapper.readTree(output);

            if (rootTree.isObject()) {
                var tree = Utils.findJsonNode(rootTree, JsonNode::isArray);

                if (tree != null)
                    return new SuggestedBook[]{objectMapper.treeToValue(tree, SuggestedBook.class)};
            }

            return objectMapper.treeToValue(rootTree, SuggestedBook[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

        var prompt = new Prompt(List.of(SEARCH_PROMPT, userContext), searchPromptOptions);
        return getAndParseFunc.apply(prompt);
    }
}
