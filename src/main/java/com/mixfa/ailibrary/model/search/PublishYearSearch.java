package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public class PublishYearSearch {
    public static class LessThan extends SearchOption.ImmutableAdapter {

        public LessThan(int year) {
            super(List.of(
                    Aggregation.match(
                            Criteria.where(Book.Fields.firstPublishYear)
                                    .lte(year)
                    )
            ));
        }
    }

    public static class GreaterThan extends SearchOption.ImmutableAdapter {

        public GreaterThan(int year) {
            super(List.of(
                    Aggregation.match(
                            Criteria.where(Book.Fields.firstPublishYear)
                                    .gte(year)
                    )
            ));
        }
    }

    public static SearchOption lessThan(final int year) {
        return new PublishYearSearch.LessThan(year);
    }

    public static SearchOption greaterThan(final int year) {
        return new PublishYearSearch.GreaterThan(year);
    }
}
