package com.example.ABHA.Authentication.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
