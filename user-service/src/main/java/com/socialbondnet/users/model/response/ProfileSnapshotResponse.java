package com.socialbondnet.users.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProfileSnapshotResponse {
    private String fullName;
    private String avatarUrl;
}
