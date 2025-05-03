package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collections;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class PresentInLibraries implements SearchOption {
    protected static final LookupOperation LOOKUP_OPERATION = LookupOperation.newLookup()
            .from(Library.TABLE_NAME)
            .localField("_id")
            .foreignField(
                    fmt("{0}.{1}.$id",
                            Library.Fields.booksAvailabilities, Library.BookAvailability.Fields.book))
            .as("libraries");

    private final List<AggregationOperation> pipeline;

    protected static <T> AggregationOperation makeMatchAggregation(T v) {
        return Aggregation.match(Criteria.where("libraries._id").in(v));
    }

    public PresentInLibraries(String... names) {
        if (names == null || names.length == 0) {
            this.pipeline = List.of();
            return;
        }
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                makeMatchAggregation(names)
        );
    }

    public PresentInLibraries(Iterable<String> names) {
        if (names == null || !names.iterator().hasNext()) {
            this.pipeline = List.of();
            return;
        }
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                makeMatchAggregation(names)
        );
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

