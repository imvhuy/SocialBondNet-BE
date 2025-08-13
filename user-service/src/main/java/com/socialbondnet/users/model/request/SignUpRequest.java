package com.socialbondnet.users.model.request;

import com.socialbondnet.users.annotations.SignUpValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDateTime dateOfBirth;

    @NotBlank(message = "Giới tính không được để trống")
    private String gender;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6)
    private String otpCode;
}