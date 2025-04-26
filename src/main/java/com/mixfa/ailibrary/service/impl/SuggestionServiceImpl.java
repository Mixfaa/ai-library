package com.mixfa.ailibrary.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class SuggestionServiceImpl implements SuggestionService {
    private final UserDataService userDataService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ListOperations<String, BookRecord> listOps;
    private final SearchEngine.ForBooks bookSearchService;

    private static final UserMessage REQUEST_PROMPT = new UserMessage("Suggest 3 books to read, based on user`s read books and available books\n");
    private static final String BOOK_RECORDS_KEY = "all-books";
    private static final OpenAiChatOptions OPTIONS = OpenAiChatOptions.builder()
            .responseFormat(
                    ResponseFormat.builder()
                            .jsonSchema(
                                    JsonSchemaGenerator.generateForType(SuggestedBook[].class)
                            )
                            .type(ResponseFormat.Type.JSON_OBJECT)
                            .build())
            .build();

    public record BookRecord(
            String id,
            String description) {
        public static BookRecord create(Book book) {
            var id = book.id().toHexString();
            var title = book.localizedTitle().getOrDefault(Utils.DEFAULT_LOCALE, null);
            if (title == null) {
                var localeFirst = book.localizedTitle().keySet().stream().findFirst();
                if (localeFirst.isPresent())
                    title = book.localizedTitle().get(localeFirst.get());
            }
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
        var sb = new StringBuilder().append("Books, present in library:\n");
        if (searchOptions.isEmpty()) {
            long size = Objects.requireNonNull(listOps.size(BOOK_RECORDS_KEY));
            if (size == 0) {

                Utils.iteratePages(bookSearchService::findAll, books -> {
                    var bookRecords = books.getContent().stream().map(BookRecord::create).toArray(BookRecord[]::new);
                    listOps.leftPushAll(BOOK_RECORDS_KEY, bookRecords);

                    for (BookRecord bookRecord : bookRecords)
                        sb.append(bookRecord.description);

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

                for (BookRecord bookRecord : bookRecords)
                    sb.append(bookRecord.description);

                return true;
            });
        }
        return sb.toString();
    }

    @Override
    public SuggestedBook[] getSuggestions() {
        return getSuggestions(SearchOption.empty());
    }

    private final static SuggestedBook[] EMPTY_SUGGESTION = new SuggestedBook[0];

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

    @Override
    public SuggestedBook[] getSuggestions(SearchOption searchOptions, SuggsetionHint suggsetionHint) {
        var booksContext = new UserMessage(prepareBooksContext(searchOptions));
        var userContext = new UserMessage(suggsetionHint.makeHint());

        var promptObj = new Prompt(List.of(REQUEST_PROMPT, booksContext, userContext), OPTIONS);

        System.out.println("Calling llmv with next prompt:");
        System.out.println(
                promptObj.getContents()
        );

        var text = chatModel.call(promptObj).getResult().getOutput().getText();

        System.out.println("LLM respond with next text:");
        System.out.println(text);

        try {
            return objectMapper.readValue(text, SuggestedBook[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
