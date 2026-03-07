package com.smartcity.complaint_management_system.utility;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtils {

    public String generateOtp() {
        return String.valueOf(
                new Random().nextInt(900000) + 100000
        );
    }
}
