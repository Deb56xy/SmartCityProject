package com.smartcity.complaint_management_system.controller;

import com.smartcity.complaint_management_system.dto.*;
import com.smartcity.complaint_management_system.model.EmailOtp;
import com.smartcity.complaint_management_system.model.User;
import com.smartcity.complaint_management_system.security.JwtUtils;
import com.smartcity.complaint_management_system.service.OtpService;
import com.smartcity.complaint_management_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpService otpService;

    @PostMapping("/signup")
    public Map<String, Object> signup(@Valid @RequestBody SignupRequest signupRequest){
        User user = userService.signup(signupRequest);
        return Map.of("message", "OTP sent to email",
                "userId", user.getId(),
                "email", user.getEmail());
    }

    @PostMapping("/verify-email-otp")
    public String verifyOtp(@RequestBody OtpVerifyRequest request) {
        otpService.verifyEmailOtp(request);
        return "Email verified successfully";
    }

    @PostMapping("/resend-email-otp-by-email")
    public Map<String, Object> requestByEmail(@RequestBody EmailRequest request) {
        Long userId = userService.resendOtpByEmail(request.getEmail(), false);
        return Map.of("userId", userId);
    }

    @PostMapping("/login")
    public Map<String,String> login(@Valid @RequestBody LoginRequest loginRequest){
        User user=userService.login(loginRequest);
        String token= jwtUtils.generateToken(user.getEmail(),user.getRole());
        return Map.of("token",token,"role",user.getRole().name());
    }

    @PostMapping("/password/forgot")
    public Map<String, Object> forgotPassword(@RequestBody EmailRequest request) {
        Long userId = userService.resendOtpByEmail(request.getEmail(), true);
        return Map.of("message", "OTP sent to email",
                "userId", userId);
    }

    @PostMapping("/password/reset")
    public String resetPassword(@RequestBody OtpVerifyRequest request) {
        userService.resetPassword(request);
        return "Password reset successful";
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(Authentication authentication) {
        String email = authentication.getName();
        return userService.getProfile(email);
    }
}
