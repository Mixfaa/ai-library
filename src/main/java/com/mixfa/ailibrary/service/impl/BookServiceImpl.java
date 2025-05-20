package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.repo.BookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepo bookRepo;
    private final MongoTemplate template;

    private final ApplicationEventPublisher eventPublisher;

    private static final Update INC_TOOK_UPD = new Update().inc(Book.Fields.tookCount, 1);
    private static final Update INC_READ_UPD = new Update().inc(Book.Fields.readCount, 1);

    public Book findBookOrThrow(Object bookId) {
        return bookRepo.findById(Utils.idToStr(bookId)).orElseThrow(() -> ExceptionType.BOOK_NOT_FOUND.make(bookId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Book addBook(Book.AddRequest request) {
        var book = bookRepo.save(new Book(
                request.title(),
                request.authors(),
                request.genres(),
                request.images(),
                request.description(),
                request.isbn(),
                request.firstPublishYear()
        ));

        eventPublisher.publishEvent(new BookService.Event.OnBookAdded(book));

        return book;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Book editBook(Object id, Book.AddRequest request) throws Exception {
        var book = findBookOrThrow(id);

        var newBook = new Book(
                book.id(),
                Objects.requireNonNullElse(request.title(), book.title()),
                Objects.requireNonNullElse(request.authors(), book.authors()),
                Objects.requireNonNullElse(request.genres(), book.genres()),
                Objects.requireNonNullElse(request.images(), book.images()),
                Objects.requireNonNullElse(request.description(), book.description()),
                book.tookCount(), book.readCount(),
                Objects.requireNonNullElse(request.isbn(),book.isbn()),
                Objects.requireNonNullElse(request.firstPublishYear(),book.firstPublishYear())
        );

        newBook = bookRepo.save(newBook);

        eventPublisher.publishEvent(new BookService.Event.OnBookEdited(newBook));
        return newBook;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void removeBook(Object id) {
        bookRepo.deleteById(Utils.idToStr(id));

        eventPublisher.publishEvent(new BookService.Event.OnBookDeleted(id));
    }

    @Override
    public void markTook(Object id) {
        var q = Query.query(Criteria.where(Book.Fields.id).is(Utils.idToObj(id)));
        template.updateFirst(q, INC_TOOK_UPD, Book.class);
    }

    @Override
    public void markRead(Object id) {
        var q = Query.query(
                Criteria.where(Book.Fields.id).is(Utils.idToObj(id))
        );

        template.updateFirst(q, INC_READ_UPD, Book.class);
    }

    @Override
    public Optional<Book> getById(Object id) {
        return bookRepo.findById(Utils.idToStr(id));
    }
}