package com.example.terminology_service.Controller;

import com.example.terminology_service.Model.NamasteCode;
import com.example.terminology_service.Service.NamasteTerminologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Internal API Controller for service-to-service communication
 * Used by FHIR Service and other microservices
 * No authentication required as it's internal communication
 */
@RestController
@RequestMapping("/internal/terminology")
@RequiredArgsConstructor
@Slf4j
public class InternalTerminologyController {

    private final NamasteTerminologyService terminologyService;

    /**
     * Internal API: Search by code for FHIR service
     * Returns raw data without wrapper response
     */
    @GetMapping("/search/code/{codeValue}")
    public ResponseEntity<List<NamasteCode>> searchByCodeInternal(@PathVariable String codeValue) {
        log.info("Internal code search request for: {}", codeValue);
        
        try {
            List<NamasteCode> results = terminologyService.searchByCode(codeValue);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in internal code search", e);
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Internal API: Search by symptoms for FHIR service
     */
    @GetMapping("/search/symptoms")
    public ResponseEntity<List<NamasteCode>> searchBySymptomsInternal(@RequestParam String query) {
        log.info("Internal symptom search request for: {}", query);
        
        try {
            List<NamasteCode> results = terminologyService.searchBySymptoms(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in internal symptom search", e);
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Internal API: Get single code by NAMASTE code
     */
    @GetMapping("/code/{namasteCode}")
    public ResponseEntity<NamasteCode> getByNamasteCodeInternal(@PathVariable String namasteCode) {
        log.info("Internal get code request for: {}", namasteCode);
        
        try {
            Optional<NamasteCode> result = terminologyService.getByNamasteCode(namasteCode);
            return result.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in internal get code", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Internal API: Get codes by category
     */
    @GetMapping("/category/{categoryType}")
    public ResponseEntity<List<NamasteCode>> getByCategoryInternal(@PathVariable String categoryType) {
        log.info("Internal category search request for: {}", categoryType);
        
        try {
            List<NamasteCode> results = terminologyService.getByCategory(categoryType);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in internal category search", e);
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Internal API: Auto-complete for other services
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<NamasteCode>> autoCompleteInternal(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Internal auto-complete request for: {} with limit: {}", query, limit);
        
        try {
            List<NamasteCode> results = terminologyService.searchForAutoComplete(query, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in internal auto-complete", e);
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Internal API: Health check for service-to-service monitoring
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthInternal() {
        return ResponseEntity.ok("TERMINOLOGY_SERVICE_UP");
    }
}
