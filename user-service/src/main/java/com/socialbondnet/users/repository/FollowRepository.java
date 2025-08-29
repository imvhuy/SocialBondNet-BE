package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.Follows;
import com.socialbondnet.users.enums.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follows, String> {

    Optional<Follows> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("SELECT COUNT(f) FROM Follows f WHERE f.followerId = :userId AND f.status = 'ACCEPTED'")
    long countFollowing(@Param("userId") String userId);

    @Query("SELECT COUNT(f) FROM Follows f WHERE f.followingId = :userId AND f.status = 'ACCEPTED'")
    long countFollowers(@Param("userId") String userId);

    @Query("SELECT COUNT(f) FROM Follows f WHERE f.followingId = :userId AND f.status = 'PENDING'")
    long countPendingRequests(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Follows f WHERE f.followerId = :followerId AND f.followingId = :followingId")
    void deleteByFollowerIdAndFollowingId(@Param("followerId") String followerId, @Param("followingId") String followingId);
}
