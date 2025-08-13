package com.socialbondnet.users.repository;

import com.socialbondnet.users.constants.OtpType;
import com.socialbondnet.users.entity.Otps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otps, Long> {
    int countByEmailAndCreatedAtAfter(String email, LocalDateTime createdAtAfter);

    void deleteByEmailAndOtpType(String email, OtpType otpType);

    Optional<Otps> findByEmailAndOtpCodeAndUsedAndExpiresAtAfter(String email, String otpCode, boolean used, LocalDateTime expiresAtAfter);
}
