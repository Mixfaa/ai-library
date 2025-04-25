package com.mixfa.ailibrary.model.search;

import java.util.List;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LibContainsBook implements SearchOption {
    private final Book book;

    @Override
    public List<AggregationOperation> makePipeline() {
        var criteria = Criteria.where(Library.Fields.booksAvailabilities)
                .elemMatch(Criteria.where("book.$id").is(book.id()));

        return List.of(Aggregation.match(criteria));
    }
}
