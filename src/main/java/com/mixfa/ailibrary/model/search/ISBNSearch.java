package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;


public class ISBNSearch extends SearchOption.ImmutableAdapter {
    public ISBNSearch(long isbn) {
        super(List.of(
                Aggregation.match(Criteria.where(Book.Fields.isbn).is(isbn))
        ));
    }
}
