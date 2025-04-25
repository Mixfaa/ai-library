package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepo extends MongoRepository<Library, String> {
    Page<Library> findAllByNameContains(String text, org.springframework.data.domain.Pageable pageable);
}
