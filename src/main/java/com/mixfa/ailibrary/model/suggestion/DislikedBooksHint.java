package com.mixfa.ailibrary.model.suggestion;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;

public class DislikedBooksHint implements SuggsetionHint {
    private final String hint;

    public DislikedBooksHint(Book[] books) {
        var sb = new StringBuilder();

        sb.append("User DISLIKED next books:\n");

        for (Book book : books)
            Utils.appendBookDescForAi(book, sb);

        this.hint = sb.toString();
    }

    @Override
    public String makeHint() {
        return hint;
    }
}
