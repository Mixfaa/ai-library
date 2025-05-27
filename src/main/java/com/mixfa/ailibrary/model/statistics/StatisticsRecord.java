package com.mixfa.ailibrary.model.statistics;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Money;
import lombok.Builder;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@FieldNameConstants
public record StatisticsRecord(
        LocalDate from,
        LocalDate to,
        List<BookStatistics> statistics) {

    @FieldNameConstants
    @Builder
    public record BookStatistics(
            Book book,
            Money moneyPaid,
            int borrowingCount) {
    }
}
