package org.socialbondnet.postservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.socialbondnet.postservice.entity.Timeline;
import org.socialbondnet.postservice.model.event.PostCreatedEvent;
import org.socialbondnet.postservice.model.response.TimelineResponse;
import org.socialbondnet.postservice.repository.TimelineRepository;
import org.socialbondnet.postservice.service.TimelineService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {
    private final TimelineRepository timelineRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    @Override
    public void processNewPost(PostCreatedEvent event) {
        try {
            List<String> followerIds = getFollowers(event.getUserId());

            List<Timeline> timelineEntries = followerIds.stream()
                    .map(followerId -> Timeline.builder()
                            .userId(followerId)
                            .postId(event.getPostId())
                            .postAuthorId(event.getUserId())
                            .postTitle(event.getTitle())
                            .postImageUrl(event.getImageUrl())
                            .postVisibility(event.getVisibility())
                            .authorDisplayName(event.getDisplayNameSnapshot())
                            .authorAvatarUrl(event.getAvatarUrlSnapshot())
                            .postCreatedAt(event.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            Timeline ownPostTimeline = Timeline.builder()
                    .userId(event.getUserId())
                    .postId(event.getPostId())
                    .postAuthorId(event.getUserId())
                    .postTitle(event.getTitle())
                    .postImageUrl(event.getImageUrl())
                    .postVisibility(event.getVisibility())
                    .authorDisplayName(event.getDisplayNameSnapshot())
                    .authorAvatarUrl(event.getAvatarUrlSnapshot())
                    .postCreatedAt(event.getCreatedAt())
                    .build();

            timelineEntries.add(ownPostTimeline);

            timelineRepository.saveAll(timelineEntries);
        } catch (Exception e) {
            System.err.println("Error processing new post for timeline: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private List<String> getFollowers(String userId) {
        try {
            String serviceName = "user-service";
            ServiceInstance instance = discoveryClient.getInstances(serviceName).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("user-service not found"));

            String url = instance.getUri().toString() + "/api/follows/followers/" + userId;

            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("Error getting followers: " + e.getMessage());
            return List.of();
        }
    }
    @Override
    public ResponseEntity<List<TimelineResponse>> getUserTimeline(String userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("postCreatedAt").descending());
            List<Timeline> timelineList = timelineRepository.findByUserIdOrderByPostCreatedAtDesc(userId, pageable);

            List<TimelineResponse> responses = timelineList.stream()
                    .map(timeline -> TimelineResponse.builder()
                            .postId(timeline.getPostId())
                            .postAuthorId(timeline.getPostAuthorId())
                            .postTitle(timeline.getPostTitle())
                            .postImageUrl(timeline.getPostImageUrl())
                            .postVisibility(timeline.getPostVisibility())
                            .authorDisplayName(timeline.getAuthorDisplayName())
                            .authorAvatarUrl(timeline.getAuthorAvatarUrl())
                            .postCreatedAt(timeline.getPostCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
