package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;

public class ByGenresSearch implements SearchOption {

    private final AggregationOperation operation;

    public ByGenresSearch(Collection<String> genres) {
        if (genres == null || genres.isEmpty()) {
            operation = null;
            return;
        }
        operation = Aggregation.match(Criteria.where(Book.Fields.genres).in(genres));
    }

    @Override
    public List<AggregationOperation> makePipeline() {
        if (operation == null) return List.of();
        return List.of(operation);
    }
}
