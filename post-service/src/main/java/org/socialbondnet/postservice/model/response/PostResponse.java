package org.socialbondnet.postservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponse {
    private String title;
    private String imageUrl;
    private String displayNameSnapshot;
    private String avatarUrlSnapshot;
    private String visibility;
    private String status;
    private String message;
}
