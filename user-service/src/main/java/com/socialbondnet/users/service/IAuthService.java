package com.socialbondnet.users.service;

import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.SignInRequest;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.response.AuthResponse;
import com.socialbondnet.users.model.response.OtpResponse;
import org.springframework.http.ResponseEntity;

public interface IAuthService {
    ResponseEntity<String> UserSignUp(SignUpRequest signUpRequest);
    ResponseEntity<OtpResponse> sendOtpForEmailVerification(SendOtpRequest request);
    ResponseEntity<AuthResponse> signIn(SignInRequest request);
}
