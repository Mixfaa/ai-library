package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import com.mixfa.ailibrary.service.*;
import com.mixfa.ailibrary.service.repo.BookBorrowingRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Service
@RequiredArgsConstructor
public class BookBorrowingServiceImpl implements BookBorrowingService {
    private final BookPricingPolicyProvider bookPricingPolicyProvider;
    private final InvoiceProvider invoiceProvider;
    private final BookBorrowingRepo borrowingDataRepo;
    private final BookService bookService;
    private final SearchEngine.ForBorrowings bookBorrowingSearchEngine;
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void clearExpired() {
        var expirityTime = Instant.now().minusSeconds(Duration.ofDays(1).toMillis());
        var notPaid = Criteria.where(BookBorrowing.Fields.isPaid).is(false);
        var expired = Criteria.where(BookBorrowing.Fields.borrowedTime).lt(expirityTime);

        mongoTemplate.remove(new Query().addCriteria(new Criteria().andOperator(notPaid, expired)), BookBorrowing.class);
    }

    @Override
    public InvoiceProvider.InvoiceData borrowBook(Object bookId) {
        if (hasAccessToBook(bookId)) throw ExceptionType.bookAleardyBorrowed(bookId);

        var book = bookService.findBookOrThrow(bookId);
        var pricingPolicy = bookPricingPolicyProvider.getBookPricingPolicy(book);
        var price = pricingPolicy.calculatePrice(book);

        var invoice = invoiceProvider.createInvoice(price, "Borrowing book: " + book.title());

        var currentTime = Instant.now();
        var bookBorrowing = borrowingDataRepo.save(new BookBorrowing(
                ObjectId.get(),
                book,
                invoice.invoiceId(),
                price,
                false,
                Account.getAuthenticatedAccount(),
                currentTime,
                currentTime.plusSeconds(Duration.ofDays(30).toSeconds())
        ));

        return invoice;
    }

    @Override
    public boolean hasAccessToBook(Object bookId) {
        var ownerCriteria = HasOwner.ownerCriteria();
        var bookCriteria = Criteria.where(fmt("{0}.$id", BookBorrowing.Fields.book)).is(Utils.idToObj(bookId));
        var isPaidCriteria = Criteria.where(BookBorrowing.Fields.isPaid).is(true);
        var timeCriteria = Criteria.where(BookBorrowing.Fields.returnTime).gt(Instant.now());

        var isPaidCriteriaComp = new Criteria().andOperator(
                ownerCriteria,
                bookCriteria,
                isPaidCriteria,
                timeCriteria
        );
        var paidExists = mongoTemplate.exists(new Query().addCriteria(isPaidCriteriaComp), BookBorrowing.class);
        if (paidExists) return true;

        var searchCriteria = new Criteria().andOperator(
                ownerCriteria,
                bookCriteria,
                timeCriteria
        );
        var borrowing = mongoTemplate.findOne(new Query().addCriteria(searchCriteria), BookBorrowing.class);

        if (borrowing == null) return false;
        if (borrowing.isPaid()) return true;

        var invoiceStatus = invoiceProvider.getInvoiceStatus(borrowing.invoiceId());

        if (invoiceStatus != InvoiceProvider.InvoiceStatus.success)
            return false;

        borrowingDataRepo.save(borrowing.withPaid(true));
        return true;
    }
}
