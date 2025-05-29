package com.mixfa.ailibrary.service.library;

import com.mixfa.ailibrary.model.library.BookBorrowing;
import com.mixfa.ailibrary.model.finance.InvoiceData;
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
