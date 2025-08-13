package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.Profiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilesRepository extends JpaRepository<Profiles, Long> {
}
