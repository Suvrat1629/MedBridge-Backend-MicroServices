package com.example.fhir_service.client;

import com.example.fhir_service.dto.NamasteCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;

/**
 * Client service to communicate with the terminology-service internal API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TerminologyServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${terminology-service.base-url}")
    private String terminologyServiceBaseUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Search by code using the internal terminology service API
     */
    public List<NamasteCode> searchByCode(String codeValue) {
        log.info("Calling terminology service - search by code: {}", codeValue);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            List<NamasteCode> result = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/terminology/search/code/{codeValue}")
                            .build(codeValue))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();

            log.info("Received {} results from terminology service", result != null ? result.size() : 0);
            return result != null ? result : List.of();

        } catch (Exception e) {
            log.error("Error calling terminology service for code search: {}", codeValue, e);
            return List.of();
        }
    }

    /**
     * Search by symptoms using the internal terminology service API
     */
    public List<NamasteCode> searchBySymptoms(String query) {
        log.info("Calling terminology service - search by symptoms: {}", query);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            String uri = UriComponentsBuilder
                    .fromPath("/internal/terminology/search/symptoms")
                    .queryParam("query", query)
                    .toUriString();

            List<NamasteCode> result = webClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();

            log.info("Received {} results from terminology service", result != null ? result.size() : 0);
            return result != null ? result : List.of();

        } catch (Exception e) {
            log.error("Error calling terminology service for symptom search: {}", query, e);
            return List.of();
        }
    }

    /**
     * Search by symptoms with POST request (for complex symptom arrays)
     */
    public List<NamasteCode> searchBySymptoms(List<String> symptoms) {
        log.info("Calling terminology service - search by symptoms (POST): {}", symptoms);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            // For now, convert to comma-separated query and use GET
            // In future, we can extend the internal API to support POST with symptom arrays
            String query = String.join(",", symptoms);
            return searchBySymptoms(query);

        } catch (Exception e) {
            log.error("Error calling terminology service for symptom search (POST): {}", symptoms, e);
            return List.of();
        }
    }

    /**
     * Get by NAMASTE code
     */
    public NamasteCode getByNamasteCode(String namasteCode) {
        log.info("Calling terminology service - get by code: {}", namasteCode);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            NamasteCode result = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/terminology/code/{namasteCode}")
                            .build(namasteCode))
                    .retrieve()
                    .bodyToMono(NamasteCode.class)
                    .timeout(TIMEOUT)
                    .block();

            log.info("Received result from terminology service: {}", result != null ? "found" : "not found");
            return result;

        } catch (Exception e) {
            log.error("Error calling terminology service for get by code: {}", namasteCode, e);
            return null;
        }
    }

    /**
     * Get by category
     */
    public List<NamasteCode> getByCategory(String categoryType) {
        log.info("Calling terminology service - get by category: {}", categoryType);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            List<NamasteCode> result = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/terminology/category/{categoryType}")
                            .build(categoryType))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();

            log.info("Received {} results from terminology service", result != null ? result.size() : 0);
            return result != null ? result : List.of();

        } catch (Exception e) {
            log.error("Error calling terminology service for category search: {}", categoryType, e);
            return List.of();
        }
    }

    /**
     * Auto-complete search
     */
    public List<NamasteCode> autoComplete(String query, int limit) {
        log.info("Calling terminology service - auto-complete: {} with limit: {}", query, limit);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            String uri = UriComponentsBuilder
                    .fromPath("/internal/terminology/autocomplete")
                    .queryParam("query", query)
                    .queryParam("limit", limit)
                    .toUriString();

            List<NamasteCode> result = webClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();

            log.info("Received {} results from terminology service", result != null ? result.size() : 0);
            return result != null ? result : List.of();

        } catch (Exception e) {
            log.error("Error calling terminology service for auto-complete: {}", query, e);
            return List.of();
        }
    }

    /**
     * Health check
     */
    public boolean isHealthy() {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(terminologyServiceBaseUrl)
                    .build();

            String result = webClient
                    .get()
                    .uri("/internal/terminology/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return "TERMINOLOGY_SERVICE_UP".equals(result);

        } catch (Exception e) {
            log.error("Error checking terminology service health", e);
            return false;
        }
    }
}
