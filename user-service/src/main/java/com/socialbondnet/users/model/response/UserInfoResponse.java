package com.socialbondnet.users.model.response;

import com.socialbondnet.users.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private String gender;
    private LocalDateTime birthDate;
    private String avatarUrl;
    private Visibility visibility;
    private List<String> roles;
    private List<String> permissions;
    private String message;
}
