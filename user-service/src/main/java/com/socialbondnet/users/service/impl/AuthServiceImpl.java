package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.constants.OtpType;
import com.socialbondnet.users.entity.Profiles;
import com.socialbondnet.users.entity.Roles;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.SignInRequest;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.AuthResponse;
import com.socialbondnet.users.model.response.OtpResponse;
import com.socialbondnet.users.repository.ProfilesRepository;
import com.socialbondnet.users.repository.RolesRepository;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IAuthService;
import com.socialbondnet.users.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OtpService otpService;
    private final RolesRepository rolesRepository;
    private final ProfilesRepository profilesRepository;
    private final JwtServiceImpl jwtService;

    @Override
    public ResponseEntity<String> UserSignUp(SignUpRequest signUpRequest) {
        if (userRepository.existsUsersByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng");
        }
        VerifyOtpRequest otpRequest = VerifyOtpRequest.builder()
                .email(signUpRequest.getEmail())
                .otpCode(signUpRequest.getOtpCode())
                .otpType(OtpType.EMAIL_VERIFICATION)
                .build();

        boolean isValid = otpService.verifyOtp(otpRequest);
        if (!isValid) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        Roles defaultRole = rolesRepository.findByRoleName("USER");

        Users user = Users.builder()
                .email(signUpRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .roles(new HashSet<>(Set.of(defaultRole)))
                .isActive(true)
                .isPrivate(false)
                .build();
        userRepository.save(user);

        Profiles userProfile = Profiles.builder()
                .user(user)
                .gender(signUpRequest.getGender())
                .birthDate(signUpRequest.getDateOfBirth())
                .fullName(signUpRequest.getFullName())
                .build();
        profilesRepository.save(userProfile);
        return ResponseEntity.ok("Bạn đã đăng ký thành công. Vui lòng đăng nhập để tiếp tục.");
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
    @Override
    public ResponseEntity<AuthResponse> signIn(SignInRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác"));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác");
        }
        if(!user.getIsActive().equals(true)) {
            throw new RuntimeException("Tài khoản của bạn đã bị vô hiệu hóa");
        }
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build());
    }
}
