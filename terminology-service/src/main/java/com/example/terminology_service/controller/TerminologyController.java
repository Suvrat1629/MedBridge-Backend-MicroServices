package com.example.terminology_service.controller;

import com.example.terminology_service.model.NamasteCode;
import com.example.terminology_service.service.NamasteTerminologyService;
import com.example.terminology_service.dto.TerminologyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terminology")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Terminology API", description = "Traditional Medicine to ICD-11 TM2 Code Mapping Service")
public class TerminologyController {

    private final NamasteTerminologyService terminologyService;

    @GetMapping("/search/code/{codeValue}")
    @Operation(summary = "Search by Medical Code", description = "Searches both tm2_code and code fields. Returns best result per medicine type, filtered to confidence > 0.6.")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchByCode(
            @Parameter(description = "Traditional medicine code or ICD-11 TM2 code", example = "NAM001", required = true)
            @PathVariable String codeValue) {

        log.info("Code search: {}", codeValue);
        try {
            List<NamasteCode> results = terminologyService.searchByCode(codeValue);
            if (results.isEmpty()) {
                return ResponseEntity.ok(TerminologyResponse.error("Code not found: " + codeValue, "NOT_FOUND"));
            }
            return ResponseEntity.ok(TerminologyResponse.success(results));
        } catch (Exception e) {
            log.error("Code search failed for: {}", codeValue, e);
            return ResponseEntity.ok(TerminologyResponse.error("Code search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/search/symptoms")
    @Operation(summary = "Search by Symptoms", description = "Case-insensitive fuzzy search across code_description, tm2_definition, tm2_title, and code_title fields.")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchBySymptoms(
            @Parameter(description = "Symptom or clinical description", example = "fever headache", required = true)
            @RequestParam String query) {

        log.info("Symptom search: {}", query);
        try {
            return ResponseEntity.ok(TerminologyResponse.success(terminologyService.searchBySymptoms(query)));
        } catch (Exception e) {
            log.error("Symptom search failed for: {}", query, e);
            return ResponseEntity.ok(TerminologyResponse.error("Symptom search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Auto-complete Search", description = "Returns matching code titles for building real-time search UIs.")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> autoComplete(
            @Parameter(description = "Search term (minimum 2 characters)", example = "fever", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Autocomplete: {} limit={}", query, limit);
        try {
            return ResponseEntity.ok(TerminologyResponse.success(terminologyService.searchForAutoComplete(query, limit)));
        } catch (Exception e) {
            log.error("Autocomplete failed for: {}", query, e);
            return ResponseEntity.ok(TerminologyResponse.error("Autocomplete failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/category/{categoryType}")
    @Operation(summary = "Search by Category", description = "Returns all codes for a traditional medicine system (ayurveda, siddha, unani, homeopathy, yoga, naturopathy).")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> getByCategory(
            @Parameter(description = "Traditional medicine category", example = "ayurveda", required = true)
            @PathVariable String categoryType) {

        log.info("Category search: {}", categoryType);
        try {
            return ResponseEntity.ok(TerminologyResponse.success(terminologyService.getByCategory(categoryType)));
        } catch (Exception e) {
            log.error("Category search failed for: {}", categoryType, e);
            return ResponseEntity.ok(TerminologyResponse.error("Category search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "terminology-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
