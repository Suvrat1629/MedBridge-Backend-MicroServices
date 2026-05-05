package com.example.fhir_service.client;

import com.example.fhir_service.dto.NamasteCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class TerminologyServiceClient {

    private final WebClient webClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    public TerminologyServiceClient(WebClient.Builder webClientBuilder,
                                    @Value("${terminology-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public List<NamasteCode> searchByCode(String codeValue) {
        log.info("Calling terminology service - search by code: {}", codeValue);
        try {
            List<NamasteCode> result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/terminology/search/code/{codeValue}")
                            .build(codeValue))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();
            return result != null ? result : List.of();
        } catch (Exception e) {
            log.error("Error calling terminology service for code search: {}", codeValue, e);
            return List.of();
        }
    }

    public List<NamasteCode> searchBySymptoms(List<String> symptoms) {
        String query = String.join(",", symptoms);
        log.info("Calling terminology service - search by symptoms: {}", query);
        try {
            String uri = UriComponentsBuilder
                    .fromPath("/internal/terminology/search/symptoms")
                    .queryParam("query", query)
                    .toUriString();
            List<NamasteCode> result = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NamasteCode>>() {})
                    .timeout(TIMEOUT)
                    .block();
            return result != null ? result : List.of();
        } catch (Exception e) {
            log.error("Error calling terminology service for symptom search: {}", symptoms, e);
            return List.of();
        }
    }
}
