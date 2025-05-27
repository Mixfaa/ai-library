package com.mixfa.ailibrary.model.statistics;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Money;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.List;

@FieldNameConstants
public record StatisticsRecord(
        Instant from,
        Instant to,
        List<BookStatistics> statistics) {

    @FieldNameConstants
    public record BookStatistics(
            Book book,
            Money moneyPaid,
            int borrowingCount) {
    }
}
