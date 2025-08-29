package com.socialbondnet.users.model.dto;

import com.socialbondnet.users.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
    private String id;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Visibility visibility;
}
