package com.example.fhir_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for DiseaseMapping - matches the grouped response from terminology-service
 */
@Data
public class DiseaseMapping {
    @JsonProperty("tm2Code")
    private String tm2Code;

    @JsonProperty("tm2Title")
    private String tm2Title;

    @JsonProperty("tm2Definition")
    private String tm2Definition;

    @JsonProperty("similarityScore")
    private Double similarityScore;

    @JsonProperty("mappings")
    private List<NamasteCode> mappings;

    public int getMappingCount() {
        return mappings != null ? mappings.size() : 0;
    }
}
