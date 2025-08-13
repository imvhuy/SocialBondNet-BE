package com.socialbondnet.users.service;

import com.socialbondnet.users.constants.OtpType;

public interface IEmailService {
    void sendOtpEmail(String toEmail, String otpCode, OtpType otpType);
}
