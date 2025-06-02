package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.user.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends MongoRepository<Account, String> {
    Page<Account> findByUsernameContainsIgnoreCase(String username, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);
}
