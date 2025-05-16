package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

public interface BookService {
    Book addBook(Book.AddRequest request) throws Exception;

    Book editBook(Object id, Book.AddRequest request) throws Exception;

    void removeBook(Object id) throws Exception;

    void markTook(Object id); // +1 book take

    void markRead(Object id); // +1 book read

    Optional<Book> getById(Object id);

    Book findBookOrThrow(Object id);

    public static sealed interface  Event  {
        public static record OnBookAdded(Book book) implements Event {}
        public static record OnBookEdited(Book book) implements Event {}
        public static record OnBookDeleted(Object bookId) implements Event {}
    }
}
