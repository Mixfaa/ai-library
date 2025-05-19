package com.mixfa.ailibrary.model.suggestion;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.ReadBook;

public class ReadBooksHint implements SuggsetionHint {
    private final String hint;

    public ReadBooksHint(ReadBook[] books) {
        this.hint = makeHint(books);
    }

    @Override
    public String makeHint() {
        return hint;
    }

    public static String makeHint(ReadBook[] books) {
        if (books == null || books.length == 0)
            return "";

        var sb = new StringBuilder();

        sb.append("User read next books:\n");

        for (ReadBook readBook : books) {
            var book = readBook.book();
            Utils.appendBookDescForAi(book, sb);
            sb.append("User review = ").append(readBook.mark().name());
            sb.append("\n");
        }

        return sb.toString();
    }
}
