package com.example.terminology_service.Repository;

import com.example.terminology_service.Model.NamasteCode;

import java.util.List;

/**
 * Custom repository interface for advanced symptom search operations
 */
public interface NamasteCodeRepositoryCustom {

    /**
     * Search by multiple symptoms with AND logic
     * Documents must match ALL provided symptoms in either code_description or tm2_definition fields
     * Uses MongoDB aggregation for dynamic N-symptom matching
     */
    List<NamasteCode> findByAllSymptoms(List<String> symptoms);
}