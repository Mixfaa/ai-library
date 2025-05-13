package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.search.SearchOption;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.Function;

public interface AiFunctions {
    record SearchArgs(String query, int page) {
    }

    record BookIdArg(String bookId) {}

    FunctionToolCallback<SearchArgs, String> searchFunction();

    FunctionToolCallback<SearchArgs, String> searchFunctionWith(SearchOption searchOption);

    FunctionToolCallback<Void, String> usersReadBooks();

    FunctionToolCallback<Void,String> usersWaitList();

    FunctionToolCallback<BookIdArg, Void> addBookToWaitList();
}
