package com.smartcity.complaint_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {

    private long userId;
    private String otp;
    private String newPassword;
}
