package com.org.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
}
