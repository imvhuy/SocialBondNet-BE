package org.socialbondnet.postservice.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import org.socialbondnet.postservice.enums.Visibility;

import java.util.List;

@Data
@NoArgsConstructor
public class PostRequest {
    private String userId;
    private String title;
    private String imageUrl;
    private Visibility visibility;
    @JsonProperty("mentionedUserIds")
    private List<String> mentionedUserIds;
}
