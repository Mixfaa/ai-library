package com.mixfa.ailibrary.service.ai;

import com.mixfa.ailibrary.model.search.SearchOption;
import org.springframework.ai.tool.function.FunctionToolCallback;

public interface AiFunctions {
    record SearchArgs(String query, int page) {
    }

    record BookIdArg(String bookId) {
    }

    record PageArg(int page) {}

    FunctionToolCallback<PageArg, String> booksIndexFunction();

    FunctionToolCallback<SearchArgs, String> searchFunction();

    FunctionToolCallback<SearchArgs, String> searchFunctionWith(SearchOption searchOption);

    FunctionToolCallback<Void, String> usersReadBooks();

    FunctionToolCallback<Void, String> usersWaitList();

    FunctionToolCallback<BookIdArg, Void> addBookToWaitList();

    FunctionToolCallback<BookIdArg, Void> removeBookFromWaitList();

    FunctionToolCallback<BookIdArg, Boolean> isBookInWaitList();

}
