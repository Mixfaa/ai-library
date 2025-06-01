package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.library.Book;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;

public class RatingSort extends SearchOption.ImmutableAdapter {
    private static final RatingSort ASCENDING = new RatingSort(Sort.Direction.ASC);
    private static final RatingSort DESCENDING = new RatingSort(Sort.Direction.DESC);

    @Getter
    private final  Sort.Direction direction;

    private RatingSort(Sort.Direction direction) {
        super(List.of(Aggregation.sort(direction, Book.Fields.rating)));
        this.direction = direction;
    }

    public static RatingSort ascending() {
        return ASCENDING;
    }

    public static RatingSort descending() {
        return DESCENDING;
    }
}
