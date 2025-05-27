package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.model.statistics.StatisticsRecord;
import com.mixfa.ailibrary.service.CurrencyConverter;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
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

    public Money[] getAllBorrowings(LocalDate from, LocalDate to, Book book) {
        var match = Aggregation.match(new Criteria().andOperator(
                Criteria.where(fmt("{0}.$id", BookBorrowing.Fields.book)).is(book.id()),
                Criteria.where(BookBorrowing.Fields.isPaid).is(true),
                Criteria.where(BookBorrowing.Fields.borrowedTime).gte(from),
                Criteria.where(BookBorrowing.Fields.borrowedTime).lte(to)
        ));

        var project = Aggregation.project(BookBorrowing.Fields.moneyPaid).andExclude("_id");

        record Result(
                Money moneyPaid
        ) {
        }

        var res = mongoTemplate.aggregate(Aggregation.newAggregation(match, project), BookBorrowing.class, Result.class);
        return res.getMappedResults().stream().map(Result::moneyPaid).toArray(Money[]::new);
    }

    @Override
    public StatisticsRecord getStatistics(LocalDate from, LocalDate to, int targetCurrency) {
        var booksIds = allBorrowedBooksIds(from, to);

        var statisticsBlocks = new ArrayList<StatisticsRecord.BookStatistics>();

        for (Book book : booksIds) {
            var allPaidMoney = getAllBorrowings(from, to, book);

            long moneyPaid = 0;

            for (Money money : allPaidMoney) {
                var amount = money.currency() == targetCurrency ?
                        money.amount() :
                        currencyConverter.convert(money, targetCurrency).amount();
                moneyPaid += amount;
            }
            var money = new Money(targetCurrency, moneyPaid);
            statisticsBlocks.add(new StatisticsRecord.BookStatistics(book, money, allPaidMoney.length));
        }

        return new StatisticsRecord(from, to, statisticsBlocks);
    }
}
