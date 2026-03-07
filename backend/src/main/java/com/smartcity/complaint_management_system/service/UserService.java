package com.smartcity.complaint_management_system.service;

import com.smartcity.complaint_management_system.dto.LoginRequest;
import com.smartcity.complaint_management_system.dto.OtpVerifyRequest;
import com.smartcity.complaint_management_system.dto.ProfileResponse;
import com.smartcity.complaint_management_system.dto.SignupRequest;
import com.smartcity.complaint_management_system.mapper.UserProfileMapper;
import com.smartcity.complaint_management_system.model.EmailOtp;
import com.smartcity.complaint_management_system.enums.Role;
import com.smartcity.complaint_management_system.model.User;
import com.smartcity.complaint_management_system.repository.OtpRepository;
import com.smartcity.complaint_management_system.repository.UserRepository;
import com.smartcity.complaint_management_system.utility.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpUtils otpUtils;

    @Autowired
    private MailService mailService;

    public User signup(SignupRequest request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("The email is already signed in");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CITIZEN);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDistrict(request.getDistrict());
        user.setState(request.getState());
        user.setLocation(request.getLocation());
        user.setPinCode(request.getPinCode());
        user.setActive(false);
        user.setEmailVerified(false);

        userRepository.save(user);
        sendEmailOTP(user);
        return user;
    }

    private void sendEmailOTP(User user) {
        String otp = otpUtils.generateOtp();
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(emailOtp);
        mailService.sendOtpVerificationMail(emailOtp);
    }

    public User login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("The email is not found"));

        if(!user.isActive())
            throw new RuntimeException("Email not verified");

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password doesnot match");
        }
        return user;
    }

    public Long resendOtpByEmail(String emailId, boolean isForgotPassword) {
        User user = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.isEmailVerified() && !isForgotPassword)
            throw new RuntimeException("Email already verified");

        String otp = otpUtils.generateOtp();
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        emailOtp.setVerified(false);
        otpRepository.save(emailOtp);
        mailService.sendOtpVerificationMail(emailOtp);
        return user.getId();
    }

    public void resetPassword(OtpVerifyRequest request) {
        EmailOtp emailOtp = otpRepository.findTopByUserIdAndVerifiedFalseOrderByExpiresAtDesc(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(emailOtp.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP Expired");

        if(!emailOtp.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP");

        User user = emailOtp.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        emailOtp.setVerified(true);
        userRepository.save(user);
        otpRepository.save(emailOtp);
    }

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with this email Id"));
        return UserProfileMapper.mapToUserProfile(user);
    }
}
