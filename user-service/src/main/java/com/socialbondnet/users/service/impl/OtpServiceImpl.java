package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.Otps;
import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.OtpResponse;
import com.socialbondnet.users.repository.OtpRepository;
import com.socialbondnet.users.service.IEmailService;
import com.socialbondnet.users.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpRepository otpRepository;
    private final IEmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_REQUESTS_PER_MINUTE = 3;
    @Transactional
    @Override
    public OtpResponse sendOtp(SendOtpRequest request) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int recentRequests = otpRepository.countByEmailAndCreatedAtAfter(request.getEmail(), oneMinuteAgo);

        if (recentRequests >= MAX_OTP_REQUESTS_PER_MINUTE) {
            return OtpResponse.builder()
                    .message("Too many OTP requests. Please try again later.")
                    .success(false)
                    .build();
        }
        otpRepository.deleteByEmailAndOtpType(request.getEmail(), request.getOtpType());

        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);

        Otps otpEntity = Otps.builder()
                .email(request.getEmail())
                .otpCode(otpCode)
                .createdAt(now)
                .expiresAt(expiresAt)
                .otpType(request.getOtpType())
                .used(false)
                .build();

        otpRepository.save(otpEntity);

        try {
            emailService.sendOtpEmail(request.getEmail(), otpCode, request.getOtpType());

            return OtpResponse.builder()
                    .message("OTP sent successfully to your email")
                    .success(true)
                    .expiresAt(expiresAt)
                    .build();

        } catch (Exception e) {
            return OtpResponse.builder()
                    .message("Failed to send OTP. Please try again.")
                    .success(false)
                    .build();
        }
    }


    @Override
    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request) {
        Optional<Otps> otpEntityOpt = otpRepository.findByEmailAndOtpCodeAndUsedAndExpiresAtAfter(
                request.getEmail(), request.getOtpCode(),false, LocalDateTime.now());
        if (otpEntityOpt.isPresent()) {
            Otps otpEntity = otpEntityOpt.get();
            otpEntity.setUsed(true);
            otpRepository.save(otpEntity);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteAll(
                otpRepository.findAll()
                        .stream()
                        .filter(otp -> otp.getExpiresAt().isBefore(LocalDateTime.now()))
                        .toList()
        );
    }
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
}
