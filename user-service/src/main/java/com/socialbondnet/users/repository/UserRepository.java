package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsUsersByEmail(String email);
    boolean existsByEmail(String email);
}
