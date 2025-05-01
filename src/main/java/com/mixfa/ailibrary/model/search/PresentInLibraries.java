package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Objects;

import static com.mixfa.ailibrary.misc.Utils.fmt;


public class PresentInLibraries implements SearchOption {
    private static final LookupOperation LOOKUP_OPERATION = LookupOperation.newLookup()
            .from(Library.TABLE_NAME)
            .localField("_id")
            .foreignField(
                    fmt("{0}.{1}.$id",
                            Library.Fields.booksAvailabilities, Library.BookAvailability.Fields.book))
            .as("libraries");


    private final List<AggregationOperation> pipeline;

    private static <T> AggregationOperation makeMatchAggregation(T v) {
        return Aggregation.match(Criteria.where("libraries._id").in(v));
    }

    public PresentInLibraries(String... names) {
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                makeMatchAggregation(names)
        );
    }

    public PresentInLibraries(Iterable<String> names) {
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                makeMatchAggregation(names)
        );
    }

    @Override
    public List<AggregationOperation> makePipeline() {
        return pipeline;
    }

    public static SearchOption forSingleLibrary(String libname) {
        return () -> List.of(
                LOOKUP_OPERATION,
                makeMatchAggregation(libname)
        );
    }
}
