package com.example.fhir_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for NamasteCode - matches the response from terminology-service
 */
@Data
public class NamasteCode {
    @JsonProperty("id")
    private String id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("codeTitle")
    private String codeTitle;

    @JsonProperty("codeDescription")
    private String codeDescription;

    @JsonProperty("type")
    private String type;

    @JsonProperty("tm2Code")
    private String tm2Code;

    @JsonProperty("tm2Title")
    private String tm2Title;

    @JsonProperty("tm2Definition")
    private String tm2Definition;

    @JsonProperty("tm2Uri")
    private String tm2Uri;

    @JsonProperty("confidenceScore")
    private Double confidenceScore;

    // Helper methods for better readability
    public String getNamasteCode() {
        return code;
    }

    public String getNamasteName() {
        return codeTitle;
    }

    public String getNamasteDescription() {
        return codeDescription;
    }

    public String getNamasteCategory() {
        return type;
    }

    public String getIcd11Tm2Code() {
        return tm2Code;
    }

    public String getIcd11Tm2Name() {
        return tm2Title;
    }

    public String getIcd11Tm2Description() {
        return tm2Definition;
    }

    public String getIcd11Tm2Uri() {
        return tm2Uri;
    }
}
