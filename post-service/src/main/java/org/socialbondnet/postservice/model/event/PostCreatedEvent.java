package org.socialbondnet.postservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socialbondnet.postservice.enums.Visibility;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreatedEvent {
    private String postId;
    private String userId;
    private String title;
    private String imageUrl;
    private Visibility visibility;
    private String displayNameSnapshot;
    private String avatarUrlSnapshot;
    private List<String> mentionedUserIds;
    private LocalDateTime createdAt;
}