package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public class LibraryByName extends SearchOption.ImmutableAdapter {
    public LibraryByName(String query) {
        super(makePipeline(query));
    }

    private static List<AggregationOperation> makePipeline(String query) {
        return StringUtils.isBlank(query) ? List.of() : List.of(
                Aggregation.match(
                        Criteria.where(Library.Fields.name).regex(query, "i")
                )
        );
    }
}
