package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class PresentInLibraries extends SearchOption.SimpleBase {
    protected static final LookupOperation LOOKUP_OPERATION = LookupOperation.newLookup()
            .from(Library.TABLE_NAME)
            .localField("_id")
            .foreignField(
                    fmt("{0}.{1}.$id",
                            Library.Fields.booksAvailabilities, Library.BookAvailability.Fields.book))
            .as("libraries");

    private final List<AggregationOperation> pipeline;

    public PresentInLibraries(String... names) {
        if (names == null || names.length == 0) {
            this.pipeline = List.of();
            return;
        }
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                Aggregation.match(Criteria.where("libraries._id").in((Object[]) names))
        );
    }

    public PresentInLibraries(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            this.pipeline = List.of();
            return;
        }
        this.pipeline = List.of(
                LOOKUP_OPERATION,
                Aggregation.match(Criteria.where("libraries._id").in(names))
        );
    }

    @Override
    List<AggregationOperation> pipeline() {
        return pipeline;
    }
}

