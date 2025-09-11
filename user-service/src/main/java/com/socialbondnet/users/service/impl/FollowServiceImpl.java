package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.Follows;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.enums.FollowStatus;
import com.socialbondnet.users.enums.Visibility;
import com.socialbondnet.users.model.response.FollowResponse;
import com.socialbondnet.users.repository.FollowRepository;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements IFollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // Rate limiting: Map<userId, Map<targetUserId, lastActionTime>>
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> rateLimitMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_WINDOW = 5000; // 5 seconds

    @Transactional
    @Override
    public FollowResponse followUser(String followerId, String followingId) {
        // Validation
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // Rate limiting check
        if (isActionAllowed(followerId, followingId)) {
            throw new IllegalStateException("Too many requests. Please wait before trying again.");
        }

        // Check if users exist
        Users follower = userRepository.findById(followerId)
            .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        Users following = userRepository.findById(followingId)
            .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalStateException("Already following or request pending");
        }

        // Determine status based on privacy
        FollowStatus status;
        if (following.getUserProfile().getVisibility().equals(Visibility.PRIVATE)) {
            status = FollowStatus.PENDING;
        } else {
            status = FollowStatus.ACCEPTED;
        }
        LocalDateTime acceptedAt = status == FollowStatus.ACCEPTED ? LocalDateTime.now() : null;

        Follows follow = Follows.builder()
            .followerId(followerId)
            .followingId(followingId)
            .status(status)
            .acceptedAt(acceptedAt)
            .build();

        follow = followRepository.save(follow);

        String message = status == FollowStatus.PENDING ?
            "Follow request sent" : "Now following user";

        log.info("User {} {} user {}", followerId,
            status == FollowStatus.PENDING ? "requested to follow" : "followed", followingId);

        return FollowResponse.builder()
            .id(follow.getId())
            .followerId(followerId)
            .followingId(followingId)
            .status(status)
            .message(message)
            .build();
    }

    @Transactional
    @Override
    public void unfollowUser(String followerId, String followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Invalid operation");
        }

        if (isActionAllowed(followerId, followingId)) {
            throw new IllegalStateException("Too many requests. Please wait before trying again.");
        }

        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalStateException("Not following this user");
        }

        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        log.info("User {} unfollowed user {}", followerId, followingId);
    }

    @Transactional
    @Override
    public FollowResponse acceptFollowRequest(String userId, String followerId) {
        Follows follow = followRepository.findByFollowerIdAndFollowingId(followerId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        follow.setStatus(FollowStatus.ACCEPTED);
        follow.setAcceptedAt(LocalDateTime.now());
        follow = followRepository.save(follow);

        log.info("User {} accepted follow request from {}", userId, followerId);

        return FollowResponse.builder()
            .id(follow.getId())
            .followerId(followerId)
            .followingId(userId)
            .status(FollowStatus.ACCEPTED)
            .message("Follow request accepted")
            .build();
    }

    @Transactional
    @Override
    public void rejectFollowRequest(String userId, String followerId) {
        Follows follow = followRepository.findByFollowerIdAndFollowingId(followerId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        followRepository.delete(follow);
        log.info("User {} rejected follow request from {}", userId, followerId);
    }

    @Override
    public boolean isActionAllowed(String userId, String targetUserId) {
        long currentTime = System.currentTimeMillis();

        rateLimitMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        ConcurrentHashMap<String, Long> userActions = rateLimitMap.get(userId);

        Long lastActionTime = userActions.get(targetUserId);
        if (lastActionTime != null && (currentTime - lastActionTime) < RATE_LIMIT_WINDOW) {
            return true;
        }

        userActions.put(targetUserId, currentTime);
        return false;
    }

    @Override
    public boolean isFollowing(String followerId, String followingId) {
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .map(follow -> follow.getStatus() == FollowStatus.ACCEPTED)
            .orElse(false);
    }

    @Override
    public long getFollowersCount(String userId) {
        return followRepository.countFollowers(userId);
    }

    @Override
    public long getFollowingCount(String userId) {
        return followRepository.countFollowing(userId);
    }

    @Override
    public long getPendingRequestsCount(String userId) {
        return followRepository.countPendingRequests(userId);
    }

    @Override
    public ResponseEntity<List<String>> getFollowers(String userId) {
        try {
            List<Follows> follows = followRepository.findAllByFollowingId(userId);
            List<String> followerIds = follows.stream()
                    .map(Follows::getFollowerId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(followerIds);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
