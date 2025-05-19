package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.cache.ByUserCache;
import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AiFunctionsImpl implements AiFunctions {
    private final SearchEngine.ForBooks booksSearchEngine;
    private final UserDataService userDataService;
    private final BookService bookService;
    private final AiBookDescriptionService aiBookDescriptionService;
    private final ByUserCache<FunctionToolCallback<?, ?>> cache;

    private final FunctionToolCallback<SearchArgs, String> defaultSearchFunction =
            FunctionToolCallback.builder("default search", (SearchArgs args) -> makeBooksContext(SearchOption.empty(), args))
                    .inputType(SearchArgs.class)
                    .description("Search for available books page by page. Params: String query(can be empty), int page(starts from 0)")
                    .build();

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
        var readBooks = userDataService.readBooks();
        return (FunctionToolCallback<Void, String>) cache.getOrPut("usersReadBooks", _ -> FunctionToolCallback.builder("getUserReadBooks", () -> {
                    var usersReadBooks = readBooks.get();
                    ArrayUtils.shuffle(usersReadBooks);
                    return aiBookDescriptionService.bookDescriptionAndMarkList(
                            Arrays.stream(usersReadBooks).limit(10).toList()
                    );
                })
                .inputType(Void.class)
                .description("Returns list of 10 random books and user`s marks, that user have read and marked")
                .build());
    }

    @Override
    public FunctionToolCallback<Void, String> usersWaitList() {
        var userWaitList = userDataService.waitList();
        return (FunctionToolCallback<Void, String>) cache.getOrPut("usersWaitList", _ -> FunctionToolCallback.builder("getUsersWaitList", () -> {
                    var usersWaitList = userWaitList.get();
                    ArrayUtils.shuffle(usersWaitList);
                    return aiBookDescriptionService.bookDescriptionList(
                            Arrays.stream(usersWaitList).limit(10).toList()
                    );
                })
                .inputType(Void.class)
                .description("Returns list of 10 random books from user`s wait list")
                .build());
    }

    private void addBookToWaitListImpl(BookIdArg args, UserDataService.WaitList waitList, boolean add) {
        var book = bookService.findBookOrThrow(args.bookId());

        waitList.acceptWriteLocked(target -> {
            if (add != target.isInList(book))
                target.addRemove(book);
        });
    }

    @Override
    public FunctionToolCallback<BookIdArg, Void> addBookToWaitList() {
        var waitList = userDataService.waitList();
        return (FunctionToolCallback<BookIdArg, Void>) cache.getOrPut("addBookToWaitList", _ ->
                FunctionToolCallback.builder("addBookToUserWaitList", (BookIdArg arg) -> addBookToWaitListImpl(arg, waitList, true))
                        .inputType(BookIdArg.class)
                        .description("Adds book to users wait list, parameter: bookId (string)")
                        .build());
    }


    @Override
    public FunctionToolCallback<BookIdArg, Void> removeBookFromWaitList() {
        var waitList = userDataService.waitList();
        return (FunctionToolCallback<BookIdArg, Void>) cache.getOrPut("removeBookFromWaitList", _ ->
                FunctionToolCallback.builder("removeBookFromWaitList", (BookIdArg arg) -> addBookToWaitListImpl(arg, waitList, false))
                        .inputType(BookIdArg.class)
                        .description("Removes book to users wait list, parameter: bookId (string)")
                        .build());
    }

    private boolean isBookInWaitListImpl(BookIdArg args, UserDataService.WaitList waitList) {
        return waitList.isInList(book -> book.id().toHexString().equals(args.bookId()));
    }

    @Override
    public FunctionToolCallback<BookIdArg, Boolean> isBookInWaitList() {
        var waitList = userDataService.waitList();
        return (FunctionToolCallback<BookIdArg, Boolean>) cache.getOrPut("isBookInWaitList", _ ->
                FunctionToolCallback.builder("isBookInWaitList", (BookIdArg arg) -> isBookInWaitListImpl(arg, waitList))
                        .inputType(BookIdArg.class)
                        .description("Checks if book is in user`s wait list, parameter: bookId (string)")
                        .build());
    }
}
