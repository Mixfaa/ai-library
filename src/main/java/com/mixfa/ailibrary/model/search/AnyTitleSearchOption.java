package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

/**
 * Search for books
 */
@RequiredArgsConstructor
public class AnyTitleSearchOption implements SearchOption {
    private final String query;

    @Override
    public List<AggregationOperation> makePipeline() {
        if (query == null || query.isBlank()) return List.of();
        var baseProjection = Book.baseProjection()
                .and(ObjectOperators.ObjectToArray.valueOfToArray(Book.Fields.localizedTitle)).as("titles");

        return List.of(
                baseProjection,
                Aggregation.match(
                        Criteria.where("titles").exists(true).and("titles.v").regex(query, "i")
                )
        );
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(query);
    }
}
