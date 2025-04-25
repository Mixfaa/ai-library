package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@RequiredArgsConstructor
class PresentInLibraries implements SearchOption {
    private final List<String> libsNames;

    private static final LookupOperation LOOKUP_OPERATION = LookupOperation.newLookup()
            .from(Library.TABLE_NAME)
            .localField("_id")
            .foreignField(
                    fmt("{0}.{1}.$id",
                            Library.Fields.booksAvailabilities, Library.BookAvailability.Fields.book))
            .as("libraries");

    @Override
    public List<AggregationOperation> makePipeline() {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("libraries._id").in(libsNames));
        return List.of(LOOKUP_OPERATION, matchOperation);
    }

    public static SearchOption forSingleLibrary(String libname) {
        return () -> {
            MatchOperation matchOperation = Aggregation.match(Criteria.where("libraries._id").in(libname));
            return List.of(LOOKUP_OPERATION, matchOperation);
        };
    }

}
