package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.BookStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookStatusRepo extends MongoRepository<BookStatus, String> {

}