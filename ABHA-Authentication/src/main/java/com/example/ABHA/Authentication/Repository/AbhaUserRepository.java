package com.example.ABHA.Authentication.Repository;

import com.example.ABHA.Authentication.Model.AbhaUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbhaUserRepository extends MongoRepository<AbhaUser, String> {
    Optional<AbhaUser> findByHealthId(String healthId);
    Optional<AbhaUser> findByHealthIdNumber(String healthIdNumber);
    Optional<AbhaUser> findByMobile(String mobile);
    boolean existsByHealthId(String healthId);
    boolean existsByMobile(String mobile);
}
