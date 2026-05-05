package com.example.terminology_service.controller;

import com.example.terminology_service.model.NamasteCode;
import com.example.terminology_service.service.NamasteTerminologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/internal/terminology")
@RequiredArgsConstructor
@Slf4j
public class InternalTerminologyController {

    private final NamasteTerminologyService terminologyService;

    @GetMapping("/search/code/{codeValue}")
    public ResponseEntity<List<NamasteCode>> searchByCodeInternal(@PathVariable String codeValue) {
        log.info("Internal code search: {}", codeValue);
        try {
            return ResponseEntity.ok(terminologyService.searchByCode(codeValue));
        } catch (Exception e) {
            log.error("Internal code search failed for: {}", codeValue, e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/search/symptoms")
    public ResponseEntity<List<NamasteCode>> searchBySymptomsInternal(@RequestParam String query) {
        log.info("Internal symptom search: {}", query);
        try {
            return ResponseEntity.ok(terminologyService.searchBySymptoms(query));
        } catch (Exception e) {
            log.error("Internal symptom search failed for: {}", query, e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/code/{namasteCode}")
    public ResponseEntity<NamasteCode> getByNamasteCodeInternal(@PathVariable String namasteCode) {
        log.info("Internal get code: {}", namasteCode);
        try {
            Optional<NamasteCode> result = terminologyService.getByNamasteCode(namasteCode);
            return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Internal get code failed for: {}", namasteCode, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{categoryType}")
    public ResponseEntity<List<NamasteCode>> getByCategoryInternal(@PathVariable String categoryType) {
        log.info("Internal category search: {}", categoryType);
        try {
            return ResponseEntity.ok(terminologyService.getByCategory(categoryType));
        } catch (Exception e) {
            log.error("Internal category search failed for: {}", categoryType, e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<NamasteCode>> autoCompleteInternal(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Internal autocomplete: {} limit={}", query, limit);
        try {
            return ResponseEntity.ok(terminologyService.searchForAutoComplete(query, limit));
        } catch (Exception e) {
            log.error("Internal autocomplete failed for: {}", query, e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthInternal() {
        return ResponseEntity.ok("TERMINOLOGY_SERVICE_UP");
    }
}
