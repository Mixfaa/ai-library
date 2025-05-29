package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.model.statistics.StatisticRecord;
import com.mixfa.ailibrary.service.CurrencyConverter;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final CurrencyConverter currencyConverter;
    private final SearchEngine.ForBorrowings borrowingsSearchEngine;
    private final MongoTemplate mongoTemplate;

    public List<Book> allBorrowedBooksIds(LocalDate from, LocalDate to) {
        var match = Query.query(new Criteria().andOperator(
                Criteria.where(BookBorrowing.Fields.isPaid).is(true),
                Criteria.where(BookBorrowing.Fields.borrowedTime).gte(from),
                Criteria.where(BookBorrowing.Fields.borrowedTime).lte(to)
        ));

        return mongoTemplate.findDistinct(match, BookBorrowing.Fields.book, BookBorrowing.class, Book.class);
    }

    private static final GroupOperation GROUP_AGGREGATION = Aggregation.group(fmt("{0}.{1}", BookBorrowing.Fields.moneyPaid, Money.Fields.currency))
            .sum(fmt("{0}.{1}", BookBorrowing.Fields.moneyPaid, Money.Fields.amount))
            .as(Money.Fields.amount);

    private static final ProjectionOperation PROJECT_AGGREGATION = Aggregation.project()
            .and("_id").as(Money.Fields.currency)
            .and(Money.Fields.amount).as(Money.Fields.amount)
            .andExclude("_id");

    public Money[] getAllBorrowings(LocalDate from, LocalDate to, Book book) {
        var match = Aggregation.match(new Criteria().andOperator(
                Criteria.where(fmt("{0}.$id", BookBorrowing.Fields.book)).is(book.id()),
                Criteria.where(BookBorrowing.Fields.isPaid).is(true),
                Criteria.where(BookBorrowing.Fields.borrowedTime).gte(from),
                Criteria.where(BookBorrowing.Fields.borrowedTime).lte(to)
        ));

        var res = mongoTemplate.aggregate(Aggregation.newAggregation(match, GROUP_AGGREGATION, PROJECT_AGGREGATION), BookBorrowing.class, Money.class);
        return res.getMappedResults().toArray(Money[]::new);
    }

    @Override
    public StatisticRecord getStatistics(LocalDate from, LocalDate to) {
        var booksIds = allBorrowedBooksIds(from, to);

        var statisticsBlocks = new ArrayList<StatisticRecord.BookStatistic>();

        for (Book book : booksIds) {
            var allPaidMoney = getAllBorrowings(from, to, book);
            statisticsBlocks.add(new StatisticRecord.BookStatistic(book, allPaidMoney, allPaidMoney.length));
        }

        return new StatisticRecord(from, to, statisticsBlocks);
    }
}
