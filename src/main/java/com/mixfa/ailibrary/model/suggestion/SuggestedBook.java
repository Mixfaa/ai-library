package com.mixfa.ailibrary.model.suggestion;

import org.bson.types.ObjectId;

public record SuggestedBook(
        String bookId,
        String title,
        String reason
) {
}
