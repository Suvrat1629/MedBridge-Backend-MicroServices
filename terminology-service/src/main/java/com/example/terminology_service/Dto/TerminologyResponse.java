package com.example.terminology_service.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TerminologyResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;
    private T data;

    public static <T> TerminologyResponse<T> success(T data) {
        return TerminologyResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> TerminologyResponse<T> error(String message, String errorCode) {
        return TerminologyResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
