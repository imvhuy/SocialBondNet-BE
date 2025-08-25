package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.SignInRequest;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.AuthResponse;
import com.socialbondnet.users.model.response.OtpResponse;
import com.socialbondnet.users.service.IAuthService;
import com.socialbondnet.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.UserSignUp(signUpRequest);
    }
    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return authService.sendOtpForEmailVerification(request);
    }
    @PostMapping("/sign-in")
    @Transactional
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        return authService.signIn(request);
    }
}
