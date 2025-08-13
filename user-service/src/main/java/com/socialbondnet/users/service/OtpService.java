package com.socialbondnet.users.service;

import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.OtpResponse;
import jakarta.transaction.Transactional;

public interface OtpService {
    OtpResponse sendOtp(SendOtpRequest request);
    boolean verifyOtp(VerifyOtpRequest otpRequest);
    void cleanupExpiredOtps();
}
