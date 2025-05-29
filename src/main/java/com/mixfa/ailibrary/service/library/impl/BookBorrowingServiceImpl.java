package com.mixfa.ailibrary.service.library.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.library.BookBorrowing;
import com.mixfa.ailibrary.model.finance.InvoiceData;
import com.mixfa.ailibrary.model.finance.InvoiceStatus;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import com.mixfa.ailibrary.service.finance.InvoiceProvider;
import com.mixfa.ailibrary.service.library.BookBorrowingService;
import com.mixfa.ailibrary.service.library.BookPricingPolicyProvider;
import com.mixfa.ailibrary.service.library.BookService;
import com.mixfa.ailibrary.service.repo.BookBorrowingRepo;
import com.mixfa.ailibrary.service.search.SearchEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final ApplicationEventPublisher eventPublisher;

    private static Criteria IS_PAID_CRITERIA = Criteria.where(BookBorrowing.Fields.isPaid).is(true);
    private static Criteria IS_NOT_PAID_CRITERIA = Criteria.where(BookBorrowing.Fields.isPaid).is(false);

    private static Criteria makeReturnTimeIsInFututreCriteria() {
        return Criteria.where(BookBorrowing.Fields.returnTime).gt(Instant.now());
    }

    @PostConstruct
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // every 12 hrs
    public void clearExpired() {
        var expirityTime = Instant.now().minusSeconds(Duration.ofDays(1).toMillis());
        var expired = Criteria.where(BookBorrowing.Fields.borrowedTime).lt(expirityTime);

        mongoTemplate.remove(new Query().addCriteria(new Criteria().andOperator(IS_NOT_PAID_CRITERIA, expired)), BookBorrowing.class);
    }

    @Override
    public InvoiceData borrowBook(Object bookId) {
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
        var isPaidCriteria = IS_PAID_CRITERIA;
        var timeCriteria = makeReturnTimeIsInFututreCriteria();

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

        if (invoiceStatus != InvoiceStatus.success)
            return false;

        borrowing = borrowingDataRepo.save(borrowing.withPaid(true));
        var bookBorrowedEvent = new BookBorrowingService.Event.OnBookBorrowed(borrowing);
        eventPublisher.publishEvent(bookBorrowedEvent);
        return true;
    }

    @Override
    public Page<BookBorrowing> findAllMyBorrowings(Pageable pageable) {
        var ownerCriteria = HasOwner.ownerCriteria();
        var timeCrtiaria = makeReturnTimeIsInFututreCriteria();

        var finalCriteria = new Criteria().andOperator(ownerCriteria, IS_PAID_CRITERIA, timeCrtiaria);
        return bookBorrowingSearchEngine.find(
                SearchOption.match(finalCriteria),
                pageable
        );
    }
}
