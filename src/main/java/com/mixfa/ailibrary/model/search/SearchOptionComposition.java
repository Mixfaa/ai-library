package com.mixfa.ailibrary.model.search;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public class SearchOptionComposition implements SearchOption {
    private final List<AggregationOperation> pipeline = new LinkedList<>();

    public SearchOptionComposition(Iterable<SearchOption> searchOptions) {
        for (var so : searchOptions) {
            if (so.isEmpty()) continue;
            pipeline.addAll(so.makePipeline());
        }
    }

    public SearchOptionComposition(SearchOption... searchOptions) {
        for (var so : searchOptions) {
            if (so.isEmpty()) continue;
            pipeline.addAll(so.makePipeline());
        }
    }

    @Override
    public List<AggregationOperation> makePipeline() {
        return pipeline;
    }

    @Override
    public boolean isEmpty() {
        return pipeline.isEmpty();
    }
}
