package org.socialbondnet.postservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileSnapshotResponse {
    private String fullName;
    private String avatarUrl;
}
