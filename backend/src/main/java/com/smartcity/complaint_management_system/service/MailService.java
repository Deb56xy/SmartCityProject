package com.smartcity.complaint_management_system.service;

import com.smartcity.complaint_management_system.model.Complaint;
import com.smartcity.complaint_management_system.model.Department;
import com.smartcity.complaint_management_system.model.EmailOtp;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${DASHBOARD_URL}")
    private String dashboardUrl;

    @Value("${REGISTERED_COMPLAINT_PATH}")
    private String registeredComplaintPath;

    @Value("${REGISTERED_COMPLAINT_AUTHORITY_PATH}")
    private String registeredComplaintAuthorityPath;

    @Value("${AUTHORITY_DASHBOARD_URL}")
    private String authorityDashboardUrl;

    @Value("${OTP_VERIFICATION_TEMPLATE_PATH}")
    private String otpVerificationTemplatePath;

    private void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // HTML enabled

            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Mail sen exception");
        }
    }

    public void sendComplaintRegisteredMail(String name, Long complaintId, String status, String to) {

        try (InputStream is = getClass().getResourceAsStream(registeredComplaintPath)) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            html = html.replace("${name}", name)
                    .replace("${complaintId}", complaintId.toString())
                    .replace("${status}", status)
                    .replace("${dashboardUrl}", dashboardUrl);

            this.sendHtmlMail(to, "Complaint Registered Successfully", html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    public void sendComplaintRegisteredAuthorityMail(Complaint complaint, String to, String name) {

        try (InputStream is = getClass().getResourceAsStream(registeredComplaintAuthorityPath)) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            html = html.replace("${complaintId}", String.valueOf(complaint.getId()))
                    .replace("${description}", complaint.getDescription())
                    .replace("${location}", complaint.getLocality())
                    .replace("${ward}", complaint.getWard())
                    .replace("${raisedBy}", name)
                    .replace("${dashboardUrl}", authorityDashboardUrl);

            this.sendHtmlMail(to, "New Complaint Registered", html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    public void sendOtpVerificationMail(EmailOtp emailOtp) {

        try (InputStream is = getClass().getResourceAsStream(otpVerificationTemplatePath)) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            html = html.replace("${otp}", emailOtp.getOtp())
                    .replace("${name}", emailOtp.getUser().getName());

            this.sendHtmlMail(emailOtp.getUser().getEmail(), "Smart City Email verification OTP", html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }
}
