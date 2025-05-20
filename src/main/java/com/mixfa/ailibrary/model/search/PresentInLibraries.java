package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class PresentInLibraries extends SearchOption.ImmutableAdapter {
    protected static final LookupOperation LOOKUP_OPERATION = LookupOperation.newLookup()
            .from(Library.TABLE_NAME)
            .localField("_id")
            .foreignField(
                    fmt("{0}.{1}.$id",
                            Library.Fields.booksAvailabilities, Library.BookAvailability.Fields.book))
            .as("libraries");

    public PresentInLibraries(String... names) {
        super(makePipeline(Arrays.asList(names)));
    }

    public PresentInLibraries(Collection<String> names) {
        super(makePipeline(names));
    }

    static List<AggregationOperation> makePipeline(Collection<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        return List.of(
                LOOKUP_OPERATION,
                Aggregation.match(Criteria.where("libraries._id").in(names))
        );
    }
}

