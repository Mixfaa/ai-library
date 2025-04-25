package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;

import java.util.Optional;

public interface BookService {
    Book addBook(Book.AddRequest request) throws Exception;

    Book editBook(Object id, Book.AddRequest request) throws Exception;

    void removeBook(Object id) throws Exception;

    void markTook(Object id); // +1 book take

    void markRead(Object id); // +1 book read

    Optional<Book> getById(Object id);

    Book findBookOrThrow(Object id);
}
