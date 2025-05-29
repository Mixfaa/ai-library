package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.library.BookBorrowing;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookBorrowingRepo extends MongoRepository<BookBorrowing, String> {
}
