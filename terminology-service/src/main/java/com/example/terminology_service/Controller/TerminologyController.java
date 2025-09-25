package com.example.terminology_service.Controller;

import com.example.terminology_service.Model.NamasteCode;
import com.example.terminology_service.Service.NamasteTerminologyService;
import com.example.terminology_service.Dto.TerminologyResponse;
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
@CrossOrigin(origins = "*")
@Tag(name = "Terminology API", description = "Traditional Medicine to ICD-11 TM2 Code Mapping")
public class TerminologyController {

    private final NamasteTerminologyService terminologyService;

    @GetMapping("/search/code/{codeValue}")
    @Operation(summary = "Search by Code",
            description = "Search for traditional medicine codes or TM2 codes. Searches in both tm2_code and code fields.")
    // Removed @Cacheable from here
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchByCode(
            @Parameter(description = "Code to search (e.g., NAM001 or XM4KH5)", required = true)
            @PathVariable String codeValue) {

        log.info("Code search request for: {}", codeValue);

        try {
            List<NamasteCode> results = terminologyService.searchByCode(codeValue);

            if (!results.isEmpty()) {
                return ResponseEntity.ok(TerminologyResponse.success(results));
            } else {
                return ResponseEntity.ok(TerminologyResponse.error("Code not found: " + codeValue, "NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Error in code search", e);
            return ResponseEntity.ok(TerminologyResponse.error("Code search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/search/symptoms")
    @Operation(summary = "Search by Symptoms",
            description = "Search for codes based on symptoms or descriptions using fuzzy matching.")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchBySymptoms(
            @Parameter(description = "Symptom or description to search (e.g., fever, headache)", required = true)
            @RequestParam String query) {

        log.info("Symptom search request for: {}", query);

        try {
            List<NamasteCode> results = terminologyService.searchBySymptoms(query);
            return ResponseEntity.ok(TerminologyResponse.success(results));
        } catch (Exception e) {
            log.error("Error in symptom search", e);
            return ResponseEntity.ok(TerminologyResponse.error("Symptom search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Auto-complete Search",
            description = "Get auto-complete suggestions for traditional medicine codes.")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> autoComplete(
            @Parameter(description = "Search term for auto-complete", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Auto-complete request for query: {} with limit: {}", query, limit);

        try {
            List<NamasteCode> results = terminologyService.searchForAutoComplete(query, limit);
            return ResponseEntity.ok(TerminologyResponse.success(results));
        } catch (Exception e) {
            log.error("Error in auto-complete search", e);
            return ResponseEntity.ok(TerminologyResponse.error("Auto-complete search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/category/{categoryType}")
    @Operation(summary = "Search by Category",
            description = "Get codes by traditional medicine category (ayurveda, siddha, unani).")
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> getByCategory(
            @Parameter(description = "Category type (ayurveda, siddha, unani)", required = true)
            @PathVariable String categoryType) {

        log.info("Category search request for: {}", categoryType);

        try {
            List<NamasteCode> results = terminologyService.getByCategory(categoryType);
            return ResponseEntity.ok(TerminologyResponse.success(results));
        } catch (Exception e) {
            log.error("Error in category search", e);
            return ResponseEntity.ok(TerminologyResponse.error("Category search failed", "SEARCH_ERROR"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the terminology service is running.")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NAMASTE Terminology Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
