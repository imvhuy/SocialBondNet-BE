package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.response.FollowResponse;
import com.socialbondnet.users.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final IFollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowResponse> followUser(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String currentUserId) {
        FollowResponse response = followService.followUser(currentUserId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String currentUserId) {

        followService.unfollowUser(currentUserId, targetUserId);
        return ResponseEntity.ok(Map.of("message", "Unfollowed successfully"));
    }

    @PostMapping("/requests/{requesterId}/accept")
    public ResponseEntity<FollowResponse> acceptFollowRequest(
            @PathVariable String requesterId,
            @RequestHeader("X-User-Id") String currentUserId) {

        FollowResponse response = followService.acceptFollowRequest(currentUserId, requesterId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{requesterId}/reject")
    public ResponseEntity<Map<String, String>> rejectFollowRequest(
            @PathVariable String requesterId,
            @RequestHeader("X-User-Id") String currentUserId) {

        followService.rejectFollowRequest(currentUserId, requesterId);
        return ResponseEntity.ok(Map.of("message", "Request rejected"));
    }

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String currentUserId) {

        boolean isFollowing = followService.isFollowing(currentUserId, targetUserId);

        return ResponseEntity.ok(Map.of(
            "isFollowing", isFollowing,
            "targetUserId", targetUserId
        ));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Long>> getFollowStats(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of(
            "followers", followService.getFollowersCount(userId),
            "following", followService.getFollowingCount(userId),
            "pendingRequests", followService.getPendingRequestsCount(userId)
        ));
    }

    @GetMapping("/relationship/{targetUserId}")
    public ResponseEntity<Map<String, Object>> getRelationship(
            @PathVariable String targetUserId,
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {

        if (currentUserId == null) {
            return ResponseEntity.ok(Map.of(
                "isFollowing", false,
                "isFollowed", false,
                "canFollow", true
            ));
        }

        boolean isFollowing = followService.isFollowing(currentUserId, targetUserId);
        boolean isFollowed = followService.isFollowing(targetUserId, currentUserId);
        boolean canFollow = !currentUserId.equals(targetUserId) && !isFollowing;

        return ResponseEntity.ok(Map.of(
            "isFollowing", isFollowing,
            "isFollowed", isFollowed,
            "canFollow", canFollow
        ));
    }
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<String>> getFollowers(@PathVariable String userId) {
        return followService.getFollowers(userId);
    }
}
