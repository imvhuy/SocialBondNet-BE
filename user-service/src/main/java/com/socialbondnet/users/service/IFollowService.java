package com.socialbondnet.users.service;


import com.socialbondnet.users.model.response.FollowResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IFollowService {
    @Transactional
    FollowResponse followUser(String followerId, String followingId);

    @Transactional
    void unfollowUser(String followerId, String followingId);

    @Transactional
    FollowResponse acceptFollowRequest(String userId, String followerId);

    @Transactional
    void rejectFollowRequest(String userId, String followerId);

    boolean isActionAllowed(String userId, String targetUserId);

    boolean isFollowing(String followerId, String followingId);

    long getFollowersCount(String userId);

    long getFollowingCount(String userId);

    long getPendingRequestsCount(String userId);

    ResponseEntity<List<String>> getFollowers(String userId);
}
