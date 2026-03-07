package com.smartcity.complaint_management_system.repository;

import com.smartcity.complaint_management_system.model.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByUserIdAndVerifiedFalseOrderByExpiresAtDesc(Long userId);
}
