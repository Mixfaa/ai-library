package com.mixfa.ailibrary.model.suggestion;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;

public class LikedBooksHint implements SuggsetionHint {
    private final String hint;

    public LikedBooksHint(String[] books) {
        if (books == null || books.length == 0) {
            hint = "";
            return;
        }
        var sb = new StringBuilder();

        sb.append("User LIKED next books:\n");

        for (String book : books)
            sb.append(book).append("\n");

        this.hint = sb.toString();
    }


    @Override
    public String makeHint() {
        return hint;
    }
}
