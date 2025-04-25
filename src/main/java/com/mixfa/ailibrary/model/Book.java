package com.mixfa.ailibrary.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mixfa.ailibrary.misc.Utils;
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
        Map<Locale, String> localizedTitle,
        String[] authors,
        Genre[] genres,
        String[] images,
        Map<Locale, String> localizedDescription,
        long tookCount,
        long readCount
) {
    public Book(Map<Locale, String> localizedTitle, String[] authors, Genre[] genres, String[] images, Map<Locale, String> localizedDescription) {
        this(ObjectId.get(), localizedTitle, authors, genres, images, localizedDescription, 0, 0);
    }

    public record AddRequest(
            Map<Locale, String> localizedTitle,
            String[] authors,
            Genre[] genres,
            String[] images,
            Map<Locale, String> localizedDescription
    ) {
    }

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
    public String titleString(Locale locale) {
        return Utils.getFromLocalizedMap(localizedTitle, locale);
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
