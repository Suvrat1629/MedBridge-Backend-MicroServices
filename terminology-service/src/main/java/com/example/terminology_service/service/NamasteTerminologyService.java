package com.example.terminology_service.service;

import com.example.terminology_service.model.NamasteCode;
import com.example.terminology_service.repository.NamasteCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NamasteTerminologyService {

    private final NamasteCodeRepository namasteCodeRepository;

    public List<NamasteCode> searchForAutoComplete(String searchTerm, int maxResults) {
        if (searchTerm == null || searchTerm.trim().length() < 2) {
            return List.of();
        }
        return namasteCodeRepository
                .findByCodeTitleContainingIgnoreCase(searchTerm.trim())
                .stream()
                .limit(maxResults)
                .toList();
    }

    public Optional<NamasteCode> getByNamasteCode(String namasteCode) {
        return namasteCodeRepository.findByCode(namasteCode);
    }

    public List<NamasteCode> getByCategory(String category) {
        return namasteCodeRepository.findByType(category);
    }

    public List<NamasteCode> searchByCode(String codeValue) {
        if (codeValue == null || codeValue.trim().isEmpty()) {
            return List.of();
        }

        String trimmedCode = codeValue.trim();

        Optional<NamasteCode> tm2doc = namasteCodeRepository.findTopByCodeOrderByConfidenceScoreDesc(trimmedCode);
        if (tm2doc.isPresent()) {
            trimmedCode = tm2doc.get().getTm2Code().trim();
        }

        List<NamasteCode> results = namasteCodeRepository.findByAnyCode(trimmedCode);
        if (results.isEmpty()) {
            return List.of();
        }

        // Keep the best result per type, filtered by confidence > 0.6
        HashMap<String, NamasteCode> bestPerType = new HashMap<>();
        for (NamasteCode code : results) {
            if (code.getConfidenceScore() == null || code.getConfidenceScore() <= 0.6) {
                continue;
            }
            bestPerType.merge(code.getType(), code, (existing, incoming) ->
                    incoming.getConfidenceScore() > existing.getConfidenceScore() ? incoming : existing);
        }

        return new ArrayList<>(bestPerType.values());
    }

    public List<NamasteCode> searchBySymptoms(String symptomQuery) {
        if (symptomQuery == null || symptomQuery.trim().length() < 2) {
            return List.of();
        }
        return namasteCodeRepository.findBySymptoms(escapeRegexSpecialChars(symptomQuery.trim()));
    }

    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]\\(\\)\\{\\}\\+\\*\\?\\^\\$\\|\\.])", "\\\\$1");
    }
}
