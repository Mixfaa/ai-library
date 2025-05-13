package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ByUserCache;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AiFunctionsImpl implements AiFunctions {
    private final SearchEngine.ForBooks booksSearchEngine;
    private final UserDataService userDataService;
    private final BookService bookService;
    private final AiBookDescriptionService aiBookDescriptionService;

    private final ByUserCache<FunctionToolCallback<?, ?>> cache = new ByUserCache<>();

    private final FunctionToolCallback<SearchArgs, String> defaultSearchFunction =
            FunctionToolCallback.builder("default search", (SearchArgs args) -> makeBooksContext(SearchOption.empty(), args))
                    .inputType(SearchArgs.class)
                    .description("Search for available books page by page. Params: String query(can be empty), int page(starts from 0)")
                    .build();

    public AiFunctionsImpl(SearchEngine.ForBooks booksSearchEngine, UserDataService userDataService, BookService bookService, AiBookDescriptionService aiBookDescriptionService) {

        this.booksSearchEngine = booksSearchEngine;
        this.userDataService = userDataService;
        this.bookService = bookService;
        this.aiBookDescriptionService = aiBookDescriptionService;
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
        var descriptions = aiBookDescriptionService.bookDescriptionList(books);

        for (String description : descriptions)
            booksResponseBuilder.append(description).append('\n');

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
        return (FunctionToolCallback<Void, String>) cache.getOrPut("usersReadBooks", _ -> FunctionToolCallback.builder("getUserReadBooks", () -> {
                    var usersReadBooks = userDataService.readBooks().get();
                    ArrayUtils.shuffle(usersReadBooks);
                    return aiBookDescriptionService.bookDescriptionAndMarkList(
                            Arrays.stream(usersReadBooks).limit(10).toList()
                    );
                })
                .description("Returns list of 10 random books and user`s marks, that user have read and marked")
                .build());
    }

    @Override
    public FunctionToolCallback<Void, String> usersWaitList() {
        return (FunctionToolCallback<Void, String>) cache.getOrPut("usersWaitList", _ -> FunctionToolCallback.builder("getUsersWaitList", () -> {
                    var usersWaitList = userDataService.waitList().get();
                    ArrayUtils.shuffle(usersWaitList);
                    return aiBookDescriptionService.bookDescriptionList(
                            Arrays.stream(usersWaitList).limit(10).toList()
                    );
                })
                .description("Returns list of 10 random books from user`s wait list")
                .build());
    }

    private void addBookToWaitListImpl(BookIdArg args) {
        var waitList = userDataService.waitList();
        var book = bookService.findBookOrThrow(args.bookId());
        if (!waitList.isInList(book))
            waitList.addRemove(book);
    }

    @Override
    public FunctionToolCallback<BookIdArg, Void> addBookToWaitList() {
        return (FunctionToolCallback<BookIdArg, Void>) cache.getOrPut("addBookToWaitList", _ ->
                FunctionToolCallback.builder("addBookToUserWaitList", this::addBookToWaitListImpl)
                        .description("Adds book to users wait list, parameter: bookId (string)")
                        .build());
    }
}
