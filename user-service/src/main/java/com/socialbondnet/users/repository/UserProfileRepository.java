package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.UserProfile;
import com.socialbondnet.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUser(Users user);
    Optional<UserProfile> findByUserId(String userId);
}
