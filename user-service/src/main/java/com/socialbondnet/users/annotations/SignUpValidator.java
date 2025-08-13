package com.socialbondnet.users.annotations;

import com.socialbondnet.users.model.request.SignUpRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

import java.time.LocalDateTime;
import java.util.Set;

public class SignUpValidator implements ConstraintValidator<SignUpValidation, SignUpRequest> {
    @Override
    public void initialize(SignUpValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    private static final Set<String> ALLOWED_GENDERS = Set.of("male", "female");
    @Override
    public boolean isValid(SignUpRequest signUpRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (signUpRequest == null) {
            return true;
        }
        constraintValidatorContext.disableDefaultConstraintViolation();

        String pwd = signUpRequest.getPassword();
        String confirm = signUpRequest.getConfirmPassword();
        if (pwd != null && confirm != null && !pwd.equals(confirm)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        if(pwd != null && !pwd.matches("^.{6,}$")){
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        LocalDateTime dob = signUpRequest.getDateOfBirth();
        if (dob != null && dob.isAfter(LocalDateTime.now())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Ngày sinh không được lớn hơn ngày hiện tại");
        }
        String gender = signUpRequest.getGender();
        if (gender != null) {
            if (!ALLOWED_GENDERS.contains(gender)) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                throw new ValidationException("Giới tính không hợp lệ");
            }
        }
        if(!signUpRequest.getFullName().matches("^\\p{L}+(?: \\p{L}+)*$")){
            throw new ValidationException("Họ tên không hợp lệ");
        }
        return true;
    }
}
