package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.impl.GenericSearchEngineImpl;
import jakarta.annotation.Nullable;
import lombok.experimental.Delegate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

public interface SearchEngine<T> {
    Page<T> findAll(Pageable pageable);

    Page<T> find(SearchOption searchOption, Pageable pageable);

    @Nullable T findOne(SearchOption searchOption);

    @Component
    class ForBooks implements SearchEngine<Book> {
        @Delegate
        private final SearchEngine<Book> eng;

        public ForBooks(MongoTemplate template) {
            this.eng = new GenericSearchEngineImpl<>(template, Book.class);
        }
    }

    @Component
    class ForLibraries implements SearchEngine<Library> {
        @Delegate
        private final SearchEngine<Library> eng;

        public ForLibraries(MongoTemplate template) {
            this.eng = new GenericSearchEngineImpl<>(template, Library.class);
        }
    }
}