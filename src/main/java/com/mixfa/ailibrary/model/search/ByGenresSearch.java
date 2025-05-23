package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;

public class ByGenresSearch extends SearchOption.ImmutableAdapter {
    public ByGenresSearch(Collection<String> genres) {
        super(makePipeline(genres));
    }

    public static List<AggregationOperation> makePipeline(Collection<String> genres) {
        if (genres == null || genres.isEmpty()) return List.of();
        return List.of(Aggregation.match(Criteria.where(Book.Fields.subjects).in(genres)));
    }
}
