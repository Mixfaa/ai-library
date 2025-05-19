package com.mixfa.ailibrary.model.search;


import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;

public class ByAuthorsSearch implements SearchOption {
    private final AggregationOperation operation;

    public ByAuthorsSearch(Collection<String> authors) {
        if (authors == null || authors.isEmpty()) {
            operation = null;
            return;
        }
        operation = Aggregation.match(Criteria.where(Book.Fields.authors).in(authors));
    }

    @Override
    public List<AggregationOperation> makePipeline() {
        if (operation == null) return List.of();
        return List.of(operation);
    }
}
