package org.socialbondnet.postservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.socialbondnet.postservice.entity.PostUserMentions;
import org.socialbondnet.postservice.entity.Posts;
import org.socialbondnet.postservice.model.response.ProfileSnapshotResponse;
import org.socialbondnet.postservice.model.request.PostRequest;
import org.socialbondnet.postservice.repository.PostRepository;
import org.socialbondnet.postservice.service.PostService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    @Override
    public ResponseEntity<String> addPost(PostRequest postRequest) {
        try {
            if ((postRequest.getTitle() == null || postRequest.getTitle().trim().isEmpty()) &&
                    (postRequest.getImageUrl() == null || postRequest.getImageUrl().trim().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body("Phải cung cấp ít nhất một trong hai trường: tiêu đề hoặc URL hình ảnh");
            }

            Set<String> allUserIds = new HashSet<>();
            allUserIds.add(postRequest.getUserId());
            if (postRequest.getMentionedUserIds() != null && !postRequest.getMentionedUserIds().isEmpty()) {
                allUserIds.addAll(postRequest.getMentionedUserIds());
            }

            Map<String, ProfileSnapshotResponse> profilesMap = getProfilesInfo(new ArrayList<>(allUserIds));

            ProfileSnapshotResponse postAuthorProfile = profilesMap.get(postRequest.getUserId());
            if (postAuthorProfile == null) {
                return ResponseEntity.internalServerError().body("Không tìm thấy thông tin của người đăng bài");
            }

            List<PostUserMentions> mentions = new ArrayList<>();
            if (postRequest.getMentionedUserIds() != null) {
                for (String mentionedUserId : postRequest.getMentionedUserIds()) {
                    ProfileSnapshotResponse mentionedUserProfile = profilesMap.get(mentionedUserId);
                    if (mentionedUserProfile != null) {
                        PostUserMentions mention = PostUserMentions.builder()
                                .mentionedUserId(mentionedUserId)
                                .displayNameSnapshot(mentionedUserProfile.getFullName())
                                .build();
                        mentions.add(mention);
                    }
                }
            }

            Posts posts = Posts.builder()
                    .avatarUrlSnapshot(postAuthorProfile.getAvatarUrl())
                    .displayNameSnapshot(postAuthorProfile.getFullName())
                    .title(postRequest.getTitle())
                    .imageUrl(postRequest.getImageUrl())
                    .userId(postRequest.getUserId())
                    .visibility(postRequest.getVisibility())
                    .mentions(mentions)
                    .build();

            for (PostUserMentions mention : mentions) {
                mention.setPost(posts);
            }

            postRepository.save(posts);

            return ResponseEntity.ok("Tạo bài viết thành công");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Đã xảy ra lỗi khi tạo bài viết: " + e.getMessage());
        }
    }
    private Map<String, ProfileSnapshotResponse> getProfilesInfo(List<String> userIds) {
        String serviceName = "user-service";
        ServiceInstance instance = discoveryClient.getInstances(serviceName).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No instances of user-service found"));

        String url = instance.getUri().toString() + "/api/profiles/snapshots?userIds=" + String.join(",", userIds);

        ResponseEntity<Map<String, ProfileSnapshotResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, ProfileSnapshotResponse>>() {}
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch profiles info");
        }
    }

}
