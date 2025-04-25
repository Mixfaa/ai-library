package com.mixfa.ailibrary.model;

import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@FieldNameConstants
@With
public record ReadBook(
        @Id @DBRef Book book,
        Mark mark
) {
    public enum Mark {
        LIKE,
        DISLIKE
    }
}

