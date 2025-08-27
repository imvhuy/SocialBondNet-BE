package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.Roles;
import com.socialbondnet.users.entity.UserProfile;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.enums.OtpType;
import com.socialbondnet.users.enums.Visibility;
import com.socialbondnet.users.model.request.RefreshTokenRequest;
import com.socialbondnet.users.model.request.SendOtpRequest;
import com.socialbondnet.users.model.request.SignInRequest;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.model.response.AuthResponse;
import com.socialbondnet.users.model.response.OtpResponse;
import com.socialbondnet.users.model.response.UserInfoResponse;
import com.socialbondnet.users.repository.UserProfileRepository;
import com.socialbondnet.users.repository.RolesRepository;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IAuthService;
import com.socialbondnet.users.service.IJwtService;
import com.socialbondnet.users.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OtpService otpService;
    private final RolesRepository rolesRepository;
    private final UserProfileRepository userProfileRepository;
    private final IJwtService jwtService;

    @Override
    @Transactional
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

        String[] s = signUpRequest.getEmail().split("@");
        String tmp = s[0];
        Users user = Users.builder()
                .email(signUpRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .roles(new HashSet<>(Set.of(defaultRole)))
                .username(tmp)
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

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build());
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest request) {
        try {
            // Validate refresh token
            if (!jwtService.isRefreshTokenValid(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .message("Refresh token không hợp lệ hoặc đã hết hạn")
                                .build());
            }

            // Get user from refresh token
            Users user = jwtService.getUserFromRefreshToken(request.getRefreshToken());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .message("Không tìm thấy người dùng")
                                .build());
            }

            // Check if user is still active
            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .message("Tài khoản đã bị vô hiệu hóa")
                                .build());
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .email(user.getEmail())
                    .message("Token đã được làm mới thành công")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Có lỗi xảy ra khi làm mới token")
                            .build());
        }
    }

    @Override
    public ResponseEntity<UserInfoResponse> getCurrentUser(String authorizationHeader) {
        try {
            // Extract token from Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(UserInfoResponse.builder()
                                .message("Token không được cung cấp")
                                .build());
            }

            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

            // Validate token
            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(UserInfoResponse.builder()
                                .message("Token không hợp lệ hoặc đã hết hạn")
                                .build());
            }

            // Get user from token
            Users user = jwtService.getUserFromAccessToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(UserInfoResponse.builder()
                                .message("Không tìm thấy người dùng")
                                .build());
            }

            // Check if user is still active
            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(UserInfoResponse.builder()
                                .message("Tài khoản đã bị vô hiệu hóa")
                                .build());
            }

            // Get user profile
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);

            // Build response
            List<String> roleNames = user.getRoles().stream()
                    .map(Roles::getRoleName)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(UserInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(profile != null ? profile.getFullName() : null)
                    .gender(profile != null ? profile.getGender() : null)
                    .birthDate(profile != null ? profile.getBirthDate() : null)
                    .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                    .isPrivate(user.getIsPrivate())
                    .roles(roleNames)
                    .permissions(getPermissionsByRoles(roleNames))
                    .message("Lấy thông tin người dùng thành công")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserInfoResponse.builder()
                            .message("Có lỗi xảy ra khi lấy thông tin người dùng")
                            .build());
        }
    }

    private List<String> getPermissionsByRoles(List<String> roles) {
        Set<String> permissions = new HashSet<>();

        for (String role : roles) {
            switch (role) {
                case "ADMIN":
                    permissions.addAll(Arrays.asList("READ", "WRITE", "DELETE", "ADMIN"));
                    break;
                case "USER":
                    permissions.addAll(Arrays.asList("READ", "WRITE"));
                    break;
                default:
                    permissions.add("READ");
            }
        }

        return new ArrayList<>(permissions);
    }
}
