package com.mixfa.ailibrary.model.search;


import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;

public class ByAuthorsSearch extends SearchOption.ImmutableAdapter {
    public ByAuthorsSearch(Collection<String> authors) {
        super(makePipeline(authors));
    }

    public static List<AggregationOperation> makePipeline(Collection<String> authors) {
        if (authors == null || authors.isEmpty())
            return List.of();

        return List.of(Aggregation.match(Criteria.where(Book.Fields.authors).in(authors)));
    }
}
