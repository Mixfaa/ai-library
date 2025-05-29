package com.mixfa.ailibrary.model.statistics;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Money;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@FieldNameConstants
@Document
public record StatisticRecord(
        @Id String title,
        LocalDate from,
        LocalDate to,
        List<BookStatistic> statistics) {

    public StatisticRecord(LocalDate from, LocalDate to, List<BookStatistic> statistics) {
        this(
                fmt("Statistic record: {0} - {1}",
                        from.format(Utils.getDateTimeFormatter()),
                        to.format(Utils.getDateTimeFormatter())),
                from, to, statistics
        );
    }

    @FieldNameConstants
    public record BookStatistic(
            Book book,
            Money[] moneyPaid,
            int borrowingCount) {

        public String moneyPaidString() {
            return Arrays.stream(moneyPaid).map(Money::asString)
                    .collect(Collectors.joining("\n"));
        }
    }
}
