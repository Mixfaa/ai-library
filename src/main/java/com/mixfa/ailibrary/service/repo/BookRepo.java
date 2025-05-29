package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.library.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepo extends MongoRepository<Book, String> {
}
