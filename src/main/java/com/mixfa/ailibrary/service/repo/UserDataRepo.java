package com.mixfa.ailibrary.service.repo;

import com.mixfa.ailibrary.model.user.UserData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepo extends MongoRepository<UserData, String> {
}
