package com.mixfa.ailibrary.model.statistics;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Money;
import lombok.Builder;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@FieldNameConstants
@Document
public record StatisticsRecord(
        @Id String title,
        LocalDate from,
        LocalDate to,
        int curencyCode,
        List<BookStatistics> statistics) {

    public StatisticsRecord(LocalDate from, LocalDate to, int currencyCode, List<BookStatistics> statistics) {
        this(
                fmt("Statistics record: {0} - {1} (currency: {2})",
                        from.format(Utils.getDateTimeFormatter()),
                        to.format(Utils.getDateTimeFormatter()),
                        Utils.findCurrencyByCodeOrThrow(currencyCode).getDisplayName()),
                from, to, currencyCode, statistics
        );
    }

    @FieldNameConstants
    @Builder
    public record BookStatistics(
            Book book,
            Money moneyPaid,
            int borrowingCount) {
    }
}
