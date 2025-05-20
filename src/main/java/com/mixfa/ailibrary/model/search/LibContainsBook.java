package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public class LibContainsBook extends SearchOption.ImmutableAdapter {
    public LibContainsBook(ObjectId id) {
        super(makePipeline(id));
    }

    public static List<AggregationOperation> makePipeline(ObjectId id) {
        if (id == null) return List.of();

        var criteria = Criteria.where(Library.Fields.booksAvailabilities)
                .elemMatch(Criteria.where("book.$id").is(id));
        return List.of(Aggregation.match(criteria));
    }
}
