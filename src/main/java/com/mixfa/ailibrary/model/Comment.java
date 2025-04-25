package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.MongoCollectionUtils;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document
@FieldNameConstants
public record Comment(
        @Id ObjectId id,
        @DBRef Book book,
        String text,
        double rate,
        Instant timestamp,
        @DBRef Account owner
) implements HasOwner {

    public Comment(Book book, String text, double rate, Account owner, Instant timestamp) {
        this(ObjectId.get(), book, text, rate, timestamp, owner);
    }

    public Comment(Book book, String text, double rate, Account owner) {
        this(ObjectId.get(), book, text, rate, Instant.now(), owner);
    }

    public record AddRequest(
            String bookId,
            String text,
            double rate
    ) {
    }

    @Transient
    public static String TABLE_NAME = MongoCollectionUtils.getPreferredCollectionName(Comment.class);
}
