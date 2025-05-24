package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.impl.GenericSearchEngineImpl;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

public interface SearchEngine<T> {
    Page<T> findAll(Pageable pageable);

    Page<T> find(SearchOption searchOption, Pageable pageable);

    @Nullable
    T findOne(SearchOption searchOption);

    @RequiredArgsConstructor
    static abstract class DelegationBase<T> implements SearchEngine<T> {
        @Delegate
        private final SearchEngine<T> searchEngine;
    }

    @Component
    static class ForBooks extends DelegationBase<Book> {
        public ForBooks(MongoTemplate template) {
            super(new GenericSearchEngineImpl<>(template, Book.class));
        }
    }

    @Component
    static class ForComments extends DelegationBase<Comment> {
        public ForComments(MongoTemplate template) {
            super(new GenericSearchEngineImpl<>(template, Comment.class));
        }
    }

    @Component
    static class ForBorrowings extends DelegationBase<BookBorrowing> {
        public ForBorrowings(MongoTemplate template) {
            super(new GenericSearchEngineImpl<>(template, BookBorrowing.class));
        }
    }
}