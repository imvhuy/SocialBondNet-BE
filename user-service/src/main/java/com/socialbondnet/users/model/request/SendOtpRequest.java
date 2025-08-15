package com.socialbondnet.users.model.request;
import com.socialbondnet.users.enums.OtpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequest {
    private String email;
    private OtpType otpType;
}