package com.socialbondnet.users.model.response;

import com.socialbondnet.users.enums.FollowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {
    private String id;
    private String followerId;
    private String followingId;
    private FollowStatus status;
    private String message;
}
