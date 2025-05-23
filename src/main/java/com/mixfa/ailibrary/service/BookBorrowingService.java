package com.mixfa.ailibrary.service;

public interface BookBorrowingService {
    InvoiceProvider.InvoiceData borrowBook(Object bookId);

    boolean hasAccessToBook(Object bookId);
}
