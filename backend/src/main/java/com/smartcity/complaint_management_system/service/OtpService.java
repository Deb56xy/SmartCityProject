package com.smartcity.complaint_management_system.service;

import com.smartcity.complaint_management_system.dto.OtpVerifyRequest;
import com.smartcity.complaint_management_system.model.EmailOtp;
import com.smartcity.complaint_management_system.model.User;
import com.smartcity.complaint_management_system.repository.OtpRepository;
import com.smartcity.complaint_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    public void verifyEmailOtp(OtpVerifyRequest request) {

        EmailOtp otp = otpRepository.findTopByUserIdAndVerifiedFalseOrderByExpiresAtDesc(request.getUserId())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if(otp.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP Expired");

        if(!otp.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP");

        otp.setVerified(true);
        otpRepository.save(otp);

        User user = otp.getUser();
        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);
    }
}
