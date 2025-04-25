package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Locale;

@Document
@With
@FieldNameConstants
public record BookStatus(
        @Id ObjectId id,
        @DBRef Book book,
        Locale locale,
        @DBRef Library library,
        @DBRef Account owner,
        Status status,
        LocalDate tookDate,
        LocalDate returnDate) implements HasOwner {

    public BookStatus(
            Book book,
            Locale locale,
            Library library,
            Account owner,
            Status status,
            LocalDate tookDate,
            LocalDate returnDate) {
        this(ObjectId.get(), book, locale, library, owner, status, tookDate, returnDate);
    }

    public enum Status {
        BOOKED,
        TOOK,
        NEED_TO_RETURN,
        RETURNED,
        CANCELLED,
    }
}
