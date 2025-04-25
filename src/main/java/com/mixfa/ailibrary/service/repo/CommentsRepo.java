package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.user.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepo extends MongoRepository<Comment, String> {
    boolean existsByBookIdAndOwner(Object bookId, Account owner);
}
