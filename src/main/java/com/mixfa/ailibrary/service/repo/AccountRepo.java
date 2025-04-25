package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.user.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepo extends MongoRepository<Account, Long> {
}
