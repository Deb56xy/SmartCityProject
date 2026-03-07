package com.smartcity.complaint_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    private String name;

    @Email
    private String email;

    @Size(min = 6)
    private String password;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String state;

    @NotBlank
    private String district;

    @NotBlank
    private String location;

    @Pattern(regexp = "\\d{6}")
    private String pinCode;
}
