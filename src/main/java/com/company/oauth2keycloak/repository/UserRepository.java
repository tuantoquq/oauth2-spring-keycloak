package com.company.oauth2keycloak.repository;

import com.company.oauth2keycloak.models.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserAccount, String> {

}
