package com.mixfa.ailibrary.model.search;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.LinkedList;
import java.util.List;

public class SearchOptionComposition extends SearchOption.ImmutableAdapter {
    public SearchOptionComposition(Iterable<SearchOption> searchOptions) {
        super(makePipeline(searchOptions));
    }

    static List<AggregationOperation> makePipeline(Iterable<SearchOption> searchOptions) {
        var result = new LinkedList<AggregationOperation>();
        for (var so : searchOptions) {
            if (so.isEmpty()) continue;
            result.addAll(so.makePipeline());
        }

        return result;
    }
}
