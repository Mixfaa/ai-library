package com.mixfa.ailibrary.model.suggestion;

import org.bson.types.ObjectId;

public record SuggestedBook(
        String bookId,
        String title,
        String reason
) {
    public SuggestedBook {
        if (bookId.isBlank())
            throw new IllegalArgumentException("bookId cannot be blank");
        if (title.isBlank())
            throw new IllegalArgumentException("title cannot be blank");
        if (reason.isBlank())
            throw new IllegalArgumentException("reason cannot be blank");
    }
}
