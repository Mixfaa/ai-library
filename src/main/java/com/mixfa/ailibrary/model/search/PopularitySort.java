package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.library.Book;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;

public class PopularitySort extends SearchOption.ImmutableAdapter {
    private static final PopularitySort ASCENDING = new PopularitySort(Sort.Direction.ASC);
    private static final PopularitySort DESCENDING = new PopularitySort(Sort.Direction.DESC);

    @Getter
    private final Sort.Direction direction;

    private PopularitySort(Sort.Direction direction) {
        super(List.of(Aggregation.sort(direction, Book.Fields.tookCount)));
        this.direction = direction;
    }

    public static PopularitySort ascending() {
        return ASCENDING;
    }

    public static PopularitySort descending() {
        return DESCENDING;
    }
}
