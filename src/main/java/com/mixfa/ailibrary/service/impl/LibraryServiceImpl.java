package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.BookStatus.Status;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.repo.BookStatusRepo;
import com.mixfa.ailibrary.service.repo.LibraryRepo;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Service
public class LibraryServiceImpl implements LibraryService {
    private final LibraryRepo libraryRepo;
    private final BookStatusRepo bookStatusRepo;
    private final MongoTemplate mongoTemplate;
    private final BookService bookService;

    private final SearchEngine<BookStatus> bookStatusSearchEngine;

    public LibraryServiceImpl(LibraryRepo libraryRepo, BookStatusRepo bookStatusRepo, MongoTemplate mongoTemplate, BookService bookService) {
        this.libraryRepo = libraryRepo;
        this.bookStatusRepo = bookStatusRepo;
        this.mongoTemplate = mongoTemplate;
        this.bookService = bookService;

        bookStatusSearchEngine = new GenericSearchEngineImpl<>(mongoTemplate, BookStatus.class);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Library registerOfflineLib(Library.AddRequest request) {
        var library = new Library(request.name(), request.address());
        return libraryRepo.save(library);
    }

    @Override
    public Library.BookAvailability[] getBooksAvailability(String libname) {
        return findOrThrow(libname).booksAvailabilities();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Library.BookAvailability[] setBookAvailability(String libname, Object bookId,
                                                          int amount) {
        var library = findOrThrow(libname);
        var bookIdObj = Utils.idToObj(bookId);

        var bookAvailabilities = library.booksAvailabilities();

        var bookAvailabilityOpt = Utils.find(bookAvailabilities,
                bookAvailability -> bookAvailability.book().id().equals(bookIdObj));

        if (bookAvailabilityOpt.isPresent()) {
            var bookAvailability = bookAvailabilityOpt.get();

            if (bookAvailability.amount() == amount)
                return bookAvailabilities;

            var updBookAvailability = bookAvailability.withAmount(amount);

            bookAvailabilities = Utils.replace(bookAvailabilities, bookAvailability, updBookAvailability);
        } else {
            var book = bookService.findBookOrThrow(bookId);
            bookAvailabilities = ArrayUtils.add(bookAvailabilities,
                    new Library.BookAvailability(book, amount));
        }

        return libraryRepo.save(library.withBooksAvailabilities(bookAvailabilities)).booksAvailabilities();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOfflineLib(String libname) {
        libraryRepo.deleteById(libname);
    }

    @Override
    public Library findOrThrow(String libname) {
        return libraryRepo.findById(libname).orElseThrow(() -> ExceptionType.libraryNotFound(libname));
    }

    @Override
    @Transactional
    public BookStatus tryOrderBook(String libname, Object bookId) {
        var bookIdObj = Utils.idToObj(bookId);
        var library = findOrThrow(libname);
        var bookAvailability = Utils.find(library.booksAvailabilities(), avail -> avail.book().id().equals(bookIdObj))
                .orElseThrow(() -> ExceptionType.noBooksAvailable(libname, bookId));

        var availableAmout = bookAvailability.amount();

        if (availableAmout == 0)
            throw ExceptionType.noBooksAvailable(libname, bookId);

        var nowDate = LocalDate.now();
        var bookStatusData = new BookStatus(
                bookAvailability.book(),
                library,
                Account.getAuthenticatedAccount(),
                BookStatus.Status.BOOKED,
                nowDate,
                nowDate.plusMonths(1));

        var updLibrary = library.withBooksAvailabilities(
                Utils.replace(library.booksAvailabilities(), bookAvailability, bookAvailability.withAmount(availableAmout - 1)));

        libraryRepo.save(updLibrary);

        return bookStatusRepo.save(bookStatusData);
    }

    @Override
    @Transactional
    public void cancelBookOrder(Object bookStatusId) {
        var query = new Query(
                new Criteria().andOperator(
                        Criteria.where(BookStatus.Fields.id).is(Utils.idToObj(bookStatusId)),
                        Criteria.where(BookStatus.Fields.status).is(Status.BOOKED),
                        HasOwner.ownerCriteria()
                )
        );

        var result = mongoTemplate.remove(query, BookStatus.class).getDeletedCount() > 0;
        if (!result) throw ExceptionType.bookStatusNotFound(bookStatusId);
    }

    private void returnBookToLibrary(BookStatus bookStatus) {
        var bookId = bookStatus.book().id();

        var library = bookStatus.library();
        var bookAvailabilityOpt = Utils.find(library.booksAvailabilities(), avail -> avail.book().id().equals(bookId));

        if (bookAvailabilityOpt.isEmpty())
            return;

        var bookAvailability = bookAvailabilityOpt.get();

        var updBookAvailability = bookAvailability.withAmount(bookAvailability.amount() + 1);

        libraryRepo.save(
                library.withBooksAvailabilities(
                        Utils.replace(library.booksAvailabilities(), bookAvailability, updBookAvailability)));
    }

    @Override
    @Transactional
    public BookStatus updateBookStatusData(Object bookStatusId, Status newStatus) {

        var bookStatus = bookStatusSearchEngine.findOne(
                SearchOption.Match.all(Criteria.where("_id").is(Utils.idToObj(bookStatusId)), HasOwner.ownerCriteria())
        );
        if (newStatus == BookStatus.Status.RETURNED) {
            bookService.markRead(bookStatus.book().id());
            returnBookToLibrary(bookStatus);
        }

        if (newStatus == BookStatus.Status.TOOK)
            bookService.markTook(bookStatus.book().id());

        return bookStatusRepo.save(bookStatus.withStatus(newStatus));
    }

    @Override
    public List<BookStatus> findAllMyTakenBooks() {
        var query = new Query(HasOwner.ownerCriteria());
        return mongoTemplate.find(query, BookStatus.class);
    }

    @Override
    public Page<BookStatus> findAllTakenBooks(String libraryId, Pageable pageable) {
        return bookStatusSearchEngine.find(
                SearchOption.match(Criteria.where(fmt("{0}.$id", BookStatus.Fields.library)).is(libraryId)),
                pageable
        );
    }

    @Override
    @Transactional
    public void editOfflineLib(String libname, Library library) {
        if (libname.equals(library.name())) {
            libraryRepo.save(library);
            return;
        }

        libraryRepo.deleteById(libname);
        libraryRepo.save(library);
    }
}