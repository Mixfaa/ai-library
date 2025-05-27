package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.invoice.InvoiceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookBorrowingService {
    InvoiceData borrowBook(Object bookId);

    boolean hasAccessToBook(Object bookId);

    Page<BookBorrowing> findAllMyBorrowings(Pageable pageable);

    public static sealed interface Event {
        public static record OnBookBorrowed(BookBorrowing bookBorrowing) implements Event {}
    }
}
