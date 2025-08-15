package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.enums.OtpType;
import com.socialbondnet.users.entity.UserProfile;
import com.socialbondnet.users.entity.Roles;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.enums.Visibility;
import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.OtpResponse;
import com.socialbondnet.users.repository.UserProfileRepository;
import com.socialbondnet.users.repository.RolesRepository;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IAuthService;
import com.socialbondnet.users.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OtpService otpService;
    private final RolesRepository rolesRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ResponseEntity<String> UserSignUp(SignUpRequest signUpRequest) {
        if (userRepository.existsUsersByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng");
        }
        VerifyOtpRequest otpRequest;
        try {
            otpRequest = VerifyOtpRequest.builder()
                    .email(signUpRequest.getEmail())
                    .otpCode(signUpRequest.getOtpCode())
                    .otpType(OtpType.EMAIL_VERIFICATION)
                    .build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        boolean isValid = otpService.verifyOtp(otpRequest);
        if (!isValid) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        Roles defaultRole = rolesRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER chưa được khởi tạo"));

        Users user = Users.builder()
                .email(signUpRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .roles(new HashSet<>(Set.of(defaultRole)))
                .isActive(true)
                .isPrivate(false)
                .build();
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setGender(signUpRequest.getGender());
        profile.setBirthDate(signUpRequest.getDateOfBirth());
        profile.setFullName(signUpRequest.getFullName());
        profile.setVisibility(Visibility.PUBLIC);
        userProfileRepository.save(profile);
        return ResponseEntity.ok("Bạn đã đăng ký thành công.");
    }
    @Override
    public ResponseEntity<OtpResponse> sendOtpForEmailVerification(SendOtpRequest request) {
        if (request.getOtpType() == OtpType.EMAIL_VERIFICATION &&
                userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(OtpResponse.builder()
                    .message("Email already registered")
                    .success(false)
                    .build(), HttpStatus.BAD_REQUEST);
        }

        OtpResponse response = otpService.sendOtp(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }


}
