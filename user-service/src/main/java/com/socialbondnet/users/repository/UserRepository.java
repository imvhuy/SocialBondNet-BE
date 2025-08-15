package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsUsersByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Users> findByEmail(String email);

}
