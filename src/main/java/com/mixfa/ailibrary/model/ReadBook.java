package com.mixfa.ailibrary.model;

import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@FieldNameConstants
@With
public record ReadBook(
        @DBRef Book book,
        Mark mark
) {
    public enum Mark {
        LIKE,
        DISLIKE
    }
}

