package com.mixfa.ailibrary.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mixfa.ailibrary.misc.Utils;
import lombok.Builder;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Document
@With
@FieldNameConstants
public record Book(
        @Id ObjectId id,
        String title,
        String[] authors,
        Genre[] genres,
        String[] images,
        String description,
        long tookCount,
        long readCount,
        long isbn,
        int firstPublishYear
) {
    public Book(String title, String[] authors, Genre[] genres, String[] images, String description, long isbn, int firstPublishYear) {
        this(ObjectId.get(), title, authors, genres, images, description, 0, 0, isbn, firstPublishYear);
    }

    public record AddRequest(
            String title,
            String[] authors,
            Genre[] genres,
            String[] images,
            String description,
            Long isbn,
            Integer firstPublishYear
    ) {
    }

    @Builder
    public record SearchRequest(
            String title,
            String[] authors,
            Genre[] genres,
            long minTookCount,
            long maxTookCount,
            long minReadCount,
            long maxReadCount
    ) {
    }

    @Transient
    @JsonIgnore
    public String authorsString() {
        return String.join(", ", authors);
    }

    @Transient
    @JsonIgnore
    public String genresString() {
        return Arrays.stream(genres).map(Genre::toString).collect(Collectors.joining(", "));
    }

    @Transient
    @JsonIgnore
    public String imageUrl() {
        return images.length == 0 ? "https://placehold.co/200x300/png" : images[0];
    }

    public boolean compareById(Book other) {
        return id.equals(other.id);
    }

    public static ProjectionOperation baseProjection() {
        return Aggregation.project(Book.class);
    }
}
