package com.socialbondnet.users.model.response;

import com.socialbondnet.users.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateProfileResponse {
    private String userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private Visibility visibility;
}
