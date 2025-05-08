package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.AiFunctions;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.UserDataService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class AiFunctionsImpl implements AiFunctions {
    private final ValueOperations<String, String> valueOps;
    private final SearchEngine.ForBooks booksSearchEngine;
    private final UserDataService userDataService;

    private final FunctionToolCallback<SearchArgs, String> defaultSearchFunction =
            FunctionToolCallback.builder("default search", (SearchArgs args) -> makeBooksContext(SearchOption.empty(), args))
                    .inputType(SearchArgs.class)
                    .description("Search for available books page by page. Params: String query(can be empty), int page(starts from 0)")
                    .build();

    public AiFunctionsImpl(SearchEngine.ForBooks booksSearchEngine, UserDataService userDataService, RedisTemplate<String, String> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
        this.booksSearchEngine = booksSearchEngine;
        this.userDataService = userDataService;
    }

    private String makeBooksContext(final SearchOption searchOption, final SearchArgs args) {
        var pageRequest = PageRequest.of(args.page(), 15);

        var query = args.query();
        Page<Book> booksPage = Page.empty();
        if (StringUtils.isNotBlank(query)) {
            var byTitleSearchOption = SearchOption.Books.byTitle(query);
            var compsedSearchOptions = searchOption.isEmpty() ? byTitleSearchOption : SearchOption.composition(byTitleSearchOption, searchOption);

            booksPage = booksSearchEngine.find(compsedSearchOptions, pageRequest);
        }
        if (booksPage.isEmpty())
            booksPage = booksSearchEngine.find(searchOption, pageRequest);
 
        var booksResponseBuilder = new StringBuilder();
        booksResponseBuilder
                .append("Page: ").append(args.page())
                .append("\nTotal pages: ").append(booksPage.getTotalPages())
                .append('\n');

        var books = booksPage.getContent();
        var descriptions = valueOps.multiGet(books.stream().map(Utils::makeBookDescriptionKey).toList());

        if (books.size() != descriptions.size())
            throw new RuntimeException("Book description count mismatch (valueOps multiGet)");

        var toSetValues = new HashMap<String, String>();
        for (int i = 0; i < books.size(); i++) {
            var book = books.get(i);
            var description = descriptions.get(i);

            if (description == null) {
                description = Utils.makeBookDescription(book);
                toSetValues.put(Utils.makeBookDescriptionKey(book), description);
            }

            booksResponseBuilder.append(description);
        }

        if (!toSetValues.isEmpty())
            valueOps.multiSet(toSetValues);

        return booksResponseBuilder.toString();
    }

    @Override
    public FunctionToolCallback<SearchArgs, String> searchFunction() {
        return defaultSearchFunction;
    }

    @Override
    public FunctionToolCallback<SearchArgs, String> searchFunctionWith(SearchOption searchOption) {
        return FunctionToolCallback.builder("search", (SearchArgs args) -> makeBooksContext(searchOption, args))
                .inputType(SearchArgs.class)
                .description("Search for available books page by page. Params: String query(can be empty), int page(starts from 0)")
                .build();
    }

    @Override
    public FunctionToolCallback<Void, String> usersReadBooks() {
        return FunctionToolCallback.builder("get user`s read books", () -> {
                    var usersReadBooks = userDataService.readBooks().get();
                    ArrayUtils.shuffle(usersReadBooks);
                    return Arrays.stream(usersReadBooks).limit(10)
                            .map(Utils::makeBookDescriptionAndMark)
                            .collect(Collectors.joining("\n"));
                })
                .description("Returns list of 10 random books and user`s marks, that user have read and marked")
                .build();
    }
}
