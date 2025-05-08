package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.search.SearchOption;
import org.springframework.ai.tool.function.FunctionToolCallback;

public interface AiFunctions {
    record SearchArgs(String query, int page) {
    }

    FunctionToolCallback<SearchArgs, String> searchFunction();

    FunctionToolCallback<SearchArgs, String> searchFunctionWith(SearchOption searchOption);

    FunctionToolCallback<Void, String> usersReadBooks();
}
