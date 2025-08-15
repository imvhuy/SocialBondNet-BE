package com.socialbondnet.users.repository;

import com.socialbondnet.users.enums.OtpType;
import com.socialbondnet.users.entity.Otps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface OtpRepository extends JpaRepository<Otps, Long> {
    int countByEmailAndCreatedAtAfter(String email, LocalDateTime createdAtAfter);

    void deleteByEmailAndOtpType(String email, OtpType otpType);

    Optional<Otps> findByEmailAndOtpCodeAndUsedAndExpiresAtAfter(String email, String otpCode, boolean used, LocalDateTime expiresAtAfter);
}
