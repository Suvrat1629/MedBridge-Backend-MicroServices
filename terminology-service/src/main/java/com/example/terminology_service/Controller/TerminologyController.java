package com.example.terminology_service.Controller;

import com.example.terminology_service.Model.NamasteCode;
import com.example.terminology_service.Service.NamasteTerminologyService;
import com.example.terminology_service.Dto.TerminologyResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Terminology API", description = "Traditional Medicine to ICD-11 TM2 Code Mapping Service for NAMASTE Healthcare System")
public class TerminologyController {

    private final NamasteTerminologyService terminologyService;

    @GetMapping("/search/code/{codeValue}")
    @Operation(
            summary = "Search by Medical Code",
            description = "Search for traditional medicine codes or ICD-11 TM2 codes. Searches in both tm2_code and code fields to provide comprehensive code mapping.",
            tags = {"Code Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found matching codes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerminologyResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Code Search",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"data\": [\n" +
                                            "    {\n" +
                                            "      \"id\": \"507f1f77bcf86cd799439011\",\n" +
                                            "      \"code\": \"NAM001\",\n" +
                                            "      \"tm2_code\": \"XM4KH5\",\n" +
                                            "      \"code_description\": \"Fever with headache\",\n" +
                                            "      \"tm2_definition\": \"Traditional medicine pattern for fever with accompanying headache\",\n" +
                                            "      \"category\": \"ayurveda\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"message\": \"Search completed successfully\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Code not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Code Not Found",
                                    value = "{\n" +
                                            "  \"success\": false,\n" +
                                            "  \"data\": null,\n" +
                                            "  \"message\": \"Code not found: INVALID123\",\n" +
                                            "  \"errorCode\": \"NOT_FOUND\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during search",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchByCode(
            @Parameter(
                    description = "Medical code to search for (supports both traditional medicine codes and TM2 codes)",
                    example = "NAM001",
                    required = true
            )
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
    @Operation(
            summary = "Search by Symptoms",
            description = "Search for medical codes based on symptoms or clinical descriptions using advanced fuzzy matching algorithms. Supports multiple symptoms and partial matching.",
            tags = {"Symptom Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found matching codes based on symptoms",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerminologyResponse.class),
                            examples = @ExampleObject(
                                    name = "Symptom Search Results",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"data\": [\n" +
                                            "    {\n" +
                                            "      \"id\": \"507f1f77bcf86cd799439011\",\n" +
                                            "      \"code\": \"NAM001\",\n" +
                                            "      \"tm2_code\": \"XM4KH5\",\n" +
                                            "      \"code_description\": \"Fever with headache and nausea\",\n" +
                                            "      \"tm2_definition\": \"Pitta imbalance causing fever with head pain\",\n" +
                                            "      \"category\": \"ayurveda\",\n" +
                                            "      \"match_score\": 0.85\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"message\": \"Found 1 matching codes\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameter",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> searchBySymptoms(
            @Parameter(
                    description = "Symptom or clinical description to search for. Supports multiple keywords and partial matching.",
                    example = "fever headache",
                    required = true
            )
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
    @Operation(
            summary = "Auto-complete Search",
            description = "Get intelligent auto-complete suggestions for traditional medicine codes and descriptions. Useful for building search interfaces with real-time suggestions.",
            tags = {"Search Assistance"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully returned auto-complete suggestions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerminologyResponse.class),
                            examples = @ExampleObject(
                                    name = "Auto-complete Results",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"data\": [\n" +
                                            "    {\n" +
                                            "      \"id\": \"507f1f77bcf86cd799439011\",\n" +
                                            "      \"code\": \"NAM001\",\n" +
                                            "      \"code_description\": \"Fever with headache\",\n" +
                                            "      \"category\": \"ayurveda\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"id\": \"507f1f77bcf86cd799439012\",\n" +
                                            "      \"code\": \"NAM002\",\n" +
                                            "      \"code_description\": \"Fever with body ache\",\n" +
                                            "      \"category\": \"ayurveda\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"message\": \"Auto-complete suggestions retrieved\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                                            "}"
                            )
                    )
            )
    })
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> autoComplete(
            @Parameter(
                    description = "Search term for auto-complete suggestions",
                    example = "fever",
                    required = true
            )
            @RequestParam String query,
            @Parameter(
                    description = "Maximum number of suggestions to return",
                    example = "10"
            )
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
    @Operation(
            summary = "Search by Traditional Medicine Category",
            description = "Retrieve medical codes filtered by traditional medicine system category. Supports Ayurveda, Siddha, Unani, and other traditional medicine systems.",
            tags = {"Category Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved codes for the specified category",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerminologyResponse.class),
                            examples = @ExampleObject(
                                    name = "Category Search Results",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"data\": [\n" +
                                            "    {\n" +
                                            "      \"id\": \"507f1f77bcf86cd799439011\",\n" +
                                            "      \"code\": \"AYU001\",\n" +
                                            "      \"tm2_code\": \"XM4KH5\",\n" +
                                            "      \"code_description\": \"Vata dosha imbalance\",\n" +
                                            "      \"tm2_definition\": \"Wind element disorder in Ayurvedic medicine\",\n" +
                                            "      \"category\": \"ayurveda\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"message\": \"Retrieved 1 codes for category: ayurveda\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid category type",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TerminologyResponse<List<NamasteCode>>> getByCategory(
            @Parameter(
                    description = "Traditional medicine category type",
                    example = "ayurveda",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ayurveda", "siddha", "unani", "homeopathy", "yoga", "naturopathy"}
                    )
            )
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

    @Hidden
    @GetMapping("/health")
    @Operation(
            summary = "Health Check",
            description = "Internal health check endpoint for service monitoring",
            tags = {"Internal"}
    )
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NAMASTE Terminology Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
