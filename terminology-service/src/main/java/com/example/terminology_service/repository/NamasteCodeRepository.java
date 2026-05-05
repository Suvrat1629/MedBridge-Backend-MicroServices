package com.example.terminology_service.repository;

import com.example.terminology_service.model.NamasteCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamasteCodeRepository extends MongoRepository<NamasteCode, String> {

    @Query(value = "{'$or': [{'tm2_code': ?0}, {'code': ?0}]}", sort = "{'confidence_score': -1}")
    List<NamasteCode> findByAnyCode(@Param("codeValue") String codeValue);

    @Query("{'$or': [" +
            "{'code_description': {$regex: ?0, $options: 'i'}}, " +
            "{'tm2_definition': {$regex: ?0, $options: 'i'}}, " +
            "{'tm2_title': {$regex: ?0, $options: 'i'}}, " +
            "{'code_title': {$regex: ?0, $options: 'i'}}" +
            "]}")
    List<NamasteCode> findBySymptoms(@Param("symptomQuery") String symptomQuery);

    Optional<NamasteCode> findByCode(String code);

    @Query("{'code_title': {$regex: ?0, $options: 'i'}}")
    List<NamasteCode> findByCodeTitleContainingIgnoreCase(@Param("query") String query);

    List<NamasteCode> findByType(String type);

    Optional<NamasteCode> findTopByCodeOrderByConfidenceScoreDesc(String code);
}
