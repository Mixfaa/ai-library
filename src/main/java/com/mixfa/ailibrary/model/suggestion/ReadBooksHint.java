package com.mixfa.ailibrary.model.suggestion;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.library.ReadBook;
import com.mixfa.ailibrary.service.ai.AiBookDescriptionService;

public class ReadBooksHint implements SuggsetionHint {
    private final String hint;

    public ReadBooksHint(ReadBook[] books, AiBookDescriptionService aiBookDescriptionService) {
        this.hint = makeHint(books, aiBookDescriptionService);
    }

    @Override
    public String makeHint() {
        return hint;
    }

    public static String makeHint(ReadBook[] books, AiBookDescriptionService aiBookDescriptionService) {
        if (books == null || books.length == 0)
            return "";

        var sb = new StringBuilder();

        sb.append("User read next books:\n");

        for (ReadBook readBook : books) {
            var book = readBook.book();
            var desc = aiBookDescriptionService.bookDescription(book);
            sb.append(desc);
            sb.append("User review = ").append(readBook.mark().name());
            sb.append("\n");
        }

        return sb.toString();
    }
}
