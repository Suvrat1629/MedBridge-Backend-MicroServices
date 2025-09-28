package com.example.fhir_service.controller;

import com.example.fhir_service.service.TerminologyFhirService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FHIR R4-COMPLIANT Controller for terminology requirements
 * ALL responses are proper FHIR resources with correct content-type
 * Delegates data operations to terminology-service via internal API
 */
@RestController
@RequestMapping("/api/fhir")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FHIR Terminology", description = "FHIR R4 compliant terminology operations for traditional medicine codes and ICD-11 TM2 mappings")
public class FhirTerminologyController {

    private final TerminologyFhirService terminologyFhirService;

    // FHIR R4 Content Type
    private static final String FHIR_JSON_CONTENT_TYPE = "application/fhir+json;fhirVersion=4.0";

    /**
     * MAIN FEATURE 1: FHIR-COMPLIANT Search by Code
     * Searches in both tm2_code and code fields, returns FHIR Parameters
     */
    @Operation(
            summary = "Search by Medical Code",
            description = "Searches for terminology entries using medical codes. Searches in both tm2_code and code fields and returns FHIR Parameters resource with matching results.",
            tags = {"Code Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found terminology entries for the given code",
                    content = @Content(
                            mediaType = "application/fhir+json;fhirVersion=4.0",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    name = "Search Results",
                                    value = "{\n" +
                                            "  \"resourceType\": \"Parameters\",\n" +
                                            "  \"id\": \"search-result-123\",\n" +
                                            "  \"parameter\": [\n" +
                                            "    {\n" +
                                            "      \"name\": \"result\",\n" +
                                            "      \"part\": [\n" +
                                            "        {\"name\": \"code\", \"valueString\": \"A01.1\"},\n" +
                                            "        {\"name\": \"tm2_code\", \"valueString\": \"TM2.001.001\"},\n" +
                                            "        {\"name\": \"description\", \"valueString\": \"Fever with headache\"}\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid code format or search failed",
                    content = @Content(
                            mediaType = "application/fhir+json;fhirVersion=4.0",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No terminology entries found for the given code",
                    content = @Content(
                            mediaType = "application/fhir+json;fhirVersion=4.0",
                            schema = @Schema(implementation = String.class)
                    )
            )
    })
    @GetMapping(value = "/search/code/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByCode(
            @Parameter(
                    description = "Medical code to search for (e.g., A01.1, TM2.001.001)",
                    example = "A01.1",
                    required = true
            )
            @PathVariable String codeValue) {

        log.info("FHIR search by code: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByCodeResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR code search", e);
            return createFhirErrorResponse("Code search failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT Search by TM2 Code Only
     * Delegates to the same search method since terminology service handles it
     */
    @Operation(
            summary = "Search by TM2 Code",
            description = "Searches for terminology entries using TM2 codes specifically (ICD-11 Traditional Medicine codes).",
            tags = {"Code Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found terminology entries for the given TM2 code",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid TM2 code format",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            )
    })
    @GetMapping(value = "/search/tm2code/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByTm2Code(
            @Parameter(
                    description = "TM2 code to search for (ICD-11 Traditional Medicine code)",
                    example = "TM2.001.001",
                    required = true
            )
            @PathVariable String codeValue) {

        log.info("FHIR search by TM2 code: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByCodeResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR TM2 code search", e);
            return createFhirErrorResponse("TM2 code search failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT Search by Code Only
     * Delegates to the same search method since terminology service handles it
     */
    @Operation(
            summary = "Search by Standard Medical Code",
            description = "Searches for terminology entries using standard medical codes (non-TM2 codes).",
            tags = {"Code Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found terminology entries for the given code",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid code format",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            )
    })
    @GetMapping(value = "/search/codeonly/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByCodeOnly(
            @Parameter(
                    description = "Standard medical code to search for",
                    example = "A01.1",
                    required = true
            )
            @PathVariable String codeValue) {
        log.info("FHIR search by code only: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByCodeResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR code only search", e);
            return createFhirErrorResponse("Code only search failed", e.getMessage());
        }
    }

    /**
     * MAIN FEATURE 2: FHIR-COMPLIANT Search by Symptoms (GET - comma-separated)
     * Searches in both code_description and tm2_definition fields, finds highest match,
     * then returns detailed results for that match's TM2 code
     * Supports: ?query=fever,headache,nausea or ?query=fever headache nausea
     */
    @Operation(
            summary = "Search by Symptoms (GET)",
            description = "Searches for terminology entries based on symptoms. Accepts comma-separated or space-separated symptoms as query parameter. Returns the best matching terminology entry with detailed FHIR Parameters.",
            tags = {"Symptom Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found terminology entries matching the symptoms",
                    content = @Content(
                            mediaType = "application/fhir+json;fhirVersion=4.0",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    name = "Symptom Search Results",
                                    value = "{\n" +
                                            "  \"resourceType\": \"Parameters\",\n" +
                                            "  \"id\": \"symptom-search-456\",\n" +
                                            "  \"parameter\": [\n" +
                                            "    {\n" +
                                            "      \"name\": \"best_match\",\n" +
                                            "      \"part\": [\n" +
                                            "        {\"name\": \"match_score\", \"valueDecimal\": 0.85},\n" +
                                            "        {\"name\": \"code\", \"valueString\": \"A01.1\"},\n" +
                                            "        {\"name\": \"tm2_code\", \"valueString\": \"TM2.001.001\"},\n" +
                                            "        {\"name\": \"description\", \"valueString\": \"Fever with headache and nausea\"}\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid symptoms query or search failed",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            )
    })
    @GetMapping(value = "/search/symptoms", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchBySymptoms(
            @Parameter(
                    description = "Symptoms to search for, separated by commas or spaces",
                    example = "fever,headache,nausea",
                    required = true
            )
            @RequestParam String query) {

        log.info("FHIR search by symptoms: {}", query);

        try {
            // Parse symptoms from comma-separated or space-separated string
            List<String> symptoms = parseSymptoms(query);
            Parameters parameters = terminologyFhirService.createSearchBySymptomsResult(symptoms);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR symptom search", e);
            return createFhirErrorResponse("Symptom search failed", e.getMessage());
        }
    }

    /**
     * MAIN FEATURE 2B: FHIR-COMPLIANT Search by Symptoms (POST - JSON array)
     * Accepts a JSON array of symptoms for more complex queries
     * Body: {"symptoms": ["fever", "headache", "nausea"]}
     */
    @Operation(
            summary = "Search by Symptoms (POST)",
            description = "Searches for terminology entries based on symptoms provided as JSON array. Allows for more complex symptom queries and better handling of multi-word symptoms.",
            tags = {"Symptom Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found terminology entries matching the symptoms",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or symptoms array missing",
                    content = @Content(mediaType = "application/fhir+json;fhirVersion=4.0")
            )
    })
    @PostMapping(value = "/search/symptoms", produces = FHIR_JSON_CONTENT_TYPE, consumes = "application/json")
    public ResponseEntity<String> searchBySymptomsPost(
            @Parameter(
                    description = "JSON object containing array of symptoms",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Symptoms Request",
                                    value = "{\n  \"symptoms\": [\"fever\", \"headache\", \"nausea\", \"body ache\"]\n}"
                            )
                    )
            )
            @RequestBody java.util.Map<String, List<String>> requestBody) {

        log.info("FHIR POST search by symptoms: {}", requestBody);

        try {
            List<String> symptoms = requestBody.get("symptoms");
            if (symptoms == null || symptoms.isEmpty()) {
                return createFhirErrorResponse("Invalid request", "symptoms array is required");
            }

            Parameters parameters = terminologyFhirService.createSearchBySymptomsResult(symptoms);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR symptom POST search", e);
            return createFhirErrorResponse("Symptom search failed", e.getMessage());
        }
    }

    /**
     * FHIR Server Capability Statement
     */
    @Hidden
    @GetMapping(value = "/metadata", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> getCapabilityStatement() {
        log.info("FHIR capability statement requested");

        try {
            CapabilityStatement capabilityStatement = createCapabilityStatement();
            String fhirJson = terminologyFhirService.toJson(capabilityStatement);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error creating capability statement", e);
            return createFhirErrorResponse("Capability statement failed", e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @Hidden
    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<java.util.Map<String, Object>> health() {
        log.info("FHIR service health check requested");

        java.util.Map<String, Object> healthStatus = new java.util.HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "fhir-service");
        healthStatus.put("timestamp", new Date());

        return ResponseEntity.ok(healthStatus);
    }

    // Helper methods

    /**
     * Helper method to parse symptoms from various formats
     */
    private List<String> parseSymptoms(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // First try comma-separated
        if (query.contains(",")) {
            return Arrays.stream(query.split(","))
                    .map(String::trim)
                    .filter(symptom -> !symptom.isEmpty())
                    .collect(Collectors.toList());
        }

        // Then try space-separated (but keep multi-word symptoms together)
        // For now, treat the entire query as one symptom if no commas
        return List.of(query.trim());
    }

    private ResponseEntity<String> createFhirResponse(String fhirJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE));
        headers.add("X-FHIR-Version", "4.0.1");
        return ResponseEntity.ok().headers(headers).body(fhirJson);
    }

    private ResponseEntity<String> createFhirErrorResponse(String message, String details) {
        try {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("error-" + System.currentTimeMillis());
            addFhirMetadata(errorOutcome);

            OperationOutcome.OperationOutcomeIssueComponent errorIssue =
                    new OperationOutcome.OperationOutcomeIssueComponent();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Error: " + message + ". Details: " + details);
            errorOutcome.addIssue(errorIssue);

            String fhirJson = terminologyFhirService.toJson(errorOutcome);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE));
            headers.add("X-FHIR-Version", "4.0.1");

            return ResponseEntity.badRequest().headers(headers).body(fhirJson);
        } catch (Exception e) {
            // Fallback to minimal FHIR error
            return ResponseEntity.badRequest()
                    .contentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE))
                    .body("{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"" + message + "\"}]}");
        }
    }

    private void addFhirMetadata(DomainResource resource) {
        Meta meta = new Meta();
        meta.setVersionId("1");
        meta.setLastUpdated(new Date());

        // Add profile for Indian EHR standards
        meta.addProfile("http://hl7.org.in/fhir/StructureDefinition/AyushTerminology");

        // Add security classification
        meta.addSecurity()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
                .setCode("N")
                .setDisplay("Normal");

        // Add terminology service tag
        meta.addTag()
                .setSystem("http://terminology.hl7.org.in/CodeSystem/terminology-tags")
                .setCode("terminology-service")
                .setDisplay("Terminology Service");

        resource.setMeta(meta);
    }

    private void addFhirMetadata(Parameters parameters) {
        Meta meta = new Meta();
        meta.setVersionId("1");
        meta.setLastUpdated(new Date());

        // Add profile for Indian EHR standards
        meta.addProfile("http://hl7.org.in/fhir/StructureDefinition/AyushParameters");

        // Add security classification
        meta.addSecurity()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
                .setCode("N")
                .setDisplay("Normal");

        // Add terminology service tag
        meta.addTag()
                .setSystem("http://terminology.hl7.org.in/CodeSystem/terminology-tags")
                .setCode("terminology-operation")
                .setDisplay("Terminology Operation");

        parameters.setMeta(meta);
    }

    private CapabilityStatement createCapabilityStatement() {
        CapabilityStatement capabilityStatement = new CapabilityStatement();
        capabilityStatement.setId("namaste-fhir-terminology-server");
        capabilityStatement.setUrl("http://terminology.hl7.org.in/fhir/CapabilityStatement/namaste-fhir-terminology-server");
        capabilityStatement.setVersion("1.0.0");
        capabilityStatement.setName("NamasteFhirTerminologyServer");
        capabilityStatement.setTitle("Namaste FHIR Terminology Server");
        capabilityStatement.setStatus(Enumerations.PublicationStatus.ACTIVE);
        capabilityStatement.setDate(new Date());
        capabilityStatement.setPublisher("Namaste Health Solutions");
        capabilityStatement.setDescription("FHIR R4 compliant terminology server for traditional medicine codes and ICD-11 TM2 mappings");
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        capabilityStatement.setSoftware(new CapabilityStatement.CapabilityStatementSoftwareComponent()
                .setName("Namaste FHIR Service")
                .setVersion("1.0.0"));
        capabilityStatement.setImplementation(new CapabilityStatement.CapabilityStatementImplementationComponent()
                .setDescription("Namaste FHIR Terminology Service Implementation")
                .setUrl("http://localhost:8083/api/fhir"));
        capabilityStatement.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        capabilityStatement.addFormat("application/fhir+json");
        capabilityStatement.addFormat("json");

        // Add REST component
        CapabilityStatement.CapabilityStatementRestComponent rest = new CapabilityStatement.CapabilityStatementRestComponent();
        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
        rest.setDocumentation("Traditional Medicine Terminology Operations");

        // Add supported operations
        rest.addOperation()
                .setName("search-by-code")
                .setDefinition("http://terminology.hl7.org.in/fhir/OperationDefinition/search-by-code");

        rest.addOperation()
                .setName("search-by-symptoms")
                .setDefinition("http://terminology.hl7.org.in/fhir/OperationDefinition/search-by-symptoms");

        capabilityStatement.addRest(rest);
        addFhirMetadata(capabilityStatement);

        return capabilityStatement;
    }
}
