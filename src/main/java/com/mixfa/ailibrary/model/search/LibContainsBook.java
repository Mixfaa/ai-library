package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class LibContainsBook implements SearchOption {
    private final List<AggregationOperation> operations;

    public LibContainsBook(ObjectId id) {
        if (id == null) {
            operations = List.of();
            return;
        }
        var criteria = Criteria.where(Library.Fields.booksAvailabilities)
                .elemMatch(Criteria.where("book.$id").is(id));
        operations = List.of(Aggregation.match(criteria));

    }

    @Override
    public List<AggregationOperation> makePipeline() {
        return operations;
    }

    @Override
    public boolean isEmpty() {
        return operations.isEmpty();
    }
}
