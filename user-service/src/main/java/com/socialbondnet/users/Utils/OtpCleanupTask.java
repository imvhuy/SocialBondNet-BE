package com.socialbondnet.users.Utils;

import com.socialbondnet.users.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpCleanupTask {
    private final OtpService otpService;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredOtps() {
        otpService.cleanupExpiredOtps();
    }
}