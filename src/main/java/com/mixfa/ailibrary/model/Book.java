package com.mixfa.ailibrary.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@With
@FieldNameConstants
public record Book(
        @Id ObjectId id,
        String title,
        String[] authors,
        String[] subjects,
        String[] images,
        String description,
        BookContentProvider contentProvider,
        long tookCount,
        long readCount,
        long isbn,
        int firstPublishYear
) {
    @Builder
    public record AddRequest(
            String title,
            String[] authors,
            String[] subjects,
            String[] images,
            String description,
            Long isbn,
            Integer firstPublishYear,
            BookContentProvider contentProvider
    ) {
    }

    @Transient
    @JsonIgnore
    public String authorsString() {
        return String.join(", ", authors);
    }

    @Transient
    @JsonIgnore
    public String subjectsString() {
        return String.join(", ", subjects);
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
