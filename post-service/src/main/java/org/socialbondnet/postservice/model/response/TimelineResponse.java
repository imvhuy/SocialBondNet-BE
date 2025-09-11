package org.socialbondnet.postservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socialbondnet.postservice.enums.Visibility;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {
    private String postId;
    private String postAuthorId;
    private String postTitle;
    private String postImageUrl;
    private Visibility postVisibility;
    private String authorDisplayName;
    private String authorAvatarUrl;
    private LocalDateTime postCreatedAt;
}