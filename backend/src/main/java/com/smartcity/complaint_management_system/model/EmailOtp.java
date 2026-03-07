package com.smartcity.complaint_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Email_otp")
@Getter
@Setter
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otp;
    private LocalDateTime expiresAt;
    private boolean verified = false;

    @ManyToOne
    private User user;
}
