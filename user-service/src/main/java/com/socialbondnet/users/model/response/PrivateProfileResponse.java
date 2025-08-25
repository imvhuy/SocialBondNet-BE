package com.socialbondnet.users.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateProfileResponse {
    private boolean isPrivate;
    private String message;
    private String username;
    private String fullName;
    private String avatarUrl;
}
