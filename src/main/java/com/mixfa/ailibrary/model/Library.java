package com.mixfa.ailibrary.model;

import jakarta.validation.constraints.NotBlank;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.MongoCollectionUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Document
@FieldNameConstants
@With
public record Library(
        @Id String name,
        String address,
        BookAvailability[] booksAvailabilities) {

    public Library(String name, String address) {
        this(name, address, new BookAvailability[0]);
    }

    public Optional<BookAvailability> findBookAvailability(Book book) {
        for (BookAvailability booksAvailability : booksAvailabilities)
            if (booksAvailability.book.id().equals(book.id()))
                return Optional.of(booksAvailability);

        return Optional.empty();
    }

    @With
    @FieldNameConstants
    public record BookAvailability(
            @DBRef Book book,
            int amount) {
    }

    public record AddRequest(
            @NotBlank String name,
            @NotBlank String address) {
    }

    public static ProjectionOperation baseProjection() {
        return Aggregation.project(Library.class);
    }

    @Transient
    public static String TABLE_NAME = MongoCollectionUtils.getPreferredCollectionName(Library.class);
}
