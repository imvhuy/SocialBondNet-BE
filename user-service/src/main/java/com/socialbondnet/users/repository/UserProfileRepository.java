package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    UserProfile findByUser_Id(String userId);

    List<UserProfile> findAllByUserIdIn(Collection<String> userIds);
}
