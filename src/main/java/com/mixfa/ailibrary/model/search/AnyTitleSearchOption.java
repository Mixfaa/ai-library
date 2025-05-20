package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

/**
 * Search for books
 */
public class AnyTitleSearchOption extends SearchOption.ImmutableAdapter {

    public AnyTitleSearchOption(String query) {
        super(makePipeline(query));
    }

    public static List<AggregationOperation> makePipeline(String query) {
        if (query == null || query.isBlank()) return List.of();
        return List.of(
                Aggregation.match(Criteria.where(Book.Fields.title).regex(query, "i"))
        );
    }
}
