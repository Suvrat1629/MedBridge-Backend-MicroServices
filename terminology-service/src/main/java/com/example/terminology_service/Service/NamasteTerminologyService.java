package com.example.terminology_service.Service;

import com.example.terminology_service.Model.NamasteCode;
import com.example.terminology_service.Repository.NamasteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NamasteTerminologyService {

    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyService.class);
    private final NamasteCodeRepository namasteCodeRepository;

    /**
     * Auto-complete search for EMR UI - Primary method for clinical workflows
     * Returns matching traditional medicine codes with their TM2 mappings
     */
    public List<NamasteCode> searchForAutoComplete(String searchTerm, int maxResults) {
        log.info("Auto-complete search for term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().length() < 2) {
            return List.of(); // Return empty list for very short search terms
        }

        List<NamasteCode> results = namasteCodeRepository.findByCodeTitleContainingIgnoreCase(searchTerm.trim());

        // Limit results for performance
        return results.stream().limit(maxResults).toList();
    }

    /**
     * Get complete details by traditional medicine code
     */
    public Optional<NamasteCode> getByNamasteCode(String namasteCode) {
        log.info("Fetching details for traditional medicine code: {}", namasteCode);
        return namasteCodeRepository.findByCode(namasteCode);
    }

    /**
     * Get codes by traditional medicine category (type)
     */
    public List<NamasteCode> getByCategory(String category) {
        log.info("Fetching codes for category: {}", category);
        return namasteCodeRepository.findByType(category);
    }

    /**
     * Search by code - Main feature 1
     * Searches in both tm2_code and code fields (EXACT MATCH ONLY)
     */
    public List<NamasteCode> searchByCode(String codeValue) {
        log.info("=== SEARCH BY CODE DEBUG START ===");
        log.info("Input codeValue: '{}'", codeValue);

        if (codeValue == null || codeValue.trim().isEmpty()) {
            log.warn("Code value is null or empty, returning empty result");
            return List.of();
        }

        String trimmedCode = codeValue.trim();
        log.info("Trimmed codeValue: '{}'", trimmedCode);

        try {
            Optional<NamasteCode> tm2docu = namasteCodeRepository.findTopByCodeOrderByConfidenceScoreDesc(trimmedCode);
            if(tm2docu.isPresent()) {
                trimmedCode = tm2docu.get().getTm2Code().trim();
            }

            Optional<List<NamasteCode>> result = namasteCodeRepository.findByAnyCode(trimmedCode);

            if (result.isEmpty() || result.get().isEmpty()) {
                log.warn("No results found for code: '{}'", trimmedCode);
                return List.of();
            }

            List<NamasteCode> results = result.get();
            log.info("Results found: {}", results.size());

            // Filter results to only include codes with confidence score > 0.6
            List<NamasteCode> filteredResults = results.stream()
                    .filter(code -> code.getConfidenceScore() != null && code.getConfidenceScore() > 0.6)
                    .toList();

            // Get best result per type
            HashMap<String, NamasteCode> finalCodes = new HashMap<>();
            for(NamasteCode code : filteredResults){
                if(!finalCodes.containsKey(code.getType())) {
                    finalCodes.put(code.getType(), code);
                } else {
                    if(finalCodes.get(code.getType()).getConfidenceScore() < code.getConfidenceScore()) {
                        finalCodes.put(code.getType(), code);
                    }
                }
            }

            filteredResults = new ArrayList<>(finalCodes.values());
            log.info("Results after confidence filter (>0.6): {}", filteredResults.size());

            log.info("=== SEARCH BY CODE DEBUG END ===");
            return filteredResults;

        } catch (Exception e) {
            log.error("Exception occurred while searching for code: '{}'", trimmedCode, e);
            throw e;
        }
    }

    /**
     * Search by symptoms/description - Main feature 2
     * Searches in both code_description and tm2_definition fields with fuzzy matching
     */
    public List<NamasteCode> searchBySymptoms(String symptomQuery) {
        log.info("Searching by symptoms/description: {}", symptomQuery);

        if (symptomQuery == null || symptomQuery.trim().length() < 2) {
            return List.of(); // Return empty list for very short search terms
        }

        // Escape special regex characters to prevent injection
        String escapedQuery = escapeRegexSpecialChars(symptomQuery.trim());

        return namasteCodeRepository.findBySymptoms(escapedQuery);
    }

    /**
     * Get all active traditional medicine codes
     */
    public List<NamasteCode> getAllActiveCodes() {
        log.info("Fetching all traditional medicine codes");
        return namasteCodeRepository.findAllByOrderByCodeTitleAsc();
    }

    /**
     * Helper method to escape special regex characters
     */
    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]\\(\\)\\{\\}\\+\\*\\?\\^\\$\\|\\.])", "\\\\$1");
    }

    /**
     * Translate traditional medicine code to ICD-11 TM2
     */
    public Optional<String> translateToIcd11Tm2(String traditionalMedicineCode) {
        log.info("Translating traditional medicine code {} to ICD-11 TM2", traditionalMedicineCode);

        return namasteCodeRepository.findByCode(traditionalMedicineCode)
                .map(NamasteCode::getTm2Code);
    }

    /**
     * Reverse translate: Find traditional medicine code from ICD-11 TM2
     */
    public Optional<NamasteCode> findByIcd11Tm2Code(String icd11Tm2Code) {
        log.info("Finding traditional medicine code for ICD-11 TM2: {}", icd11Tm2Code);
        return namasteCodeRepository.findByTm2Code(icd11Tm2Code);
    }
}
