package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.constants.OtpType;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Override
    public void sendOtpEmail(String toEmail, String otpCode, OtpType otpType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);

                String subject = getEmailSubject(otpType);
                String content = buildEmailContent(otpCode, otpType);

                helper.setSubject(subject);
                helper.setText(content, false);

                mailSender.send(message);

            } catch (MessagingException e) {
                throw new RuntimeException("Failed to configure email message", e);
            }

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to create email message", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    private String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Email Verification - OTP Code";
            case PASSWORD_RESET -> "Password Reset - OTP Code";
        };
    }

    private String buildEmailContent(String otpCode, OtpType otpType) {
        String purpose = switch (otpType) {
            case EMAIL_VERIFICATION -> "verify your email address";
            case PASSWORD_RESET -> "reset your password";
        };

        return String.format("Your OTP code to %s is: %s. This code will expire in 5 minutes.",
                purpose, otpCode);
    }
}
