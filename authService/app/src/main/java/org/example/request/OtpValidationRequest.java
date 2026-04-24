package org.example.request;

import lombok.Data;

@Data
public class OtpValidationRequest {
    private String email;
    private String otp;
}
