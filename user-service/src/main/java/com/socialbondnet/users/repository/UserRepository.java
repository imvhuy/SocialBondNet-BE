package com.socialbondnet.users.repository;

import com.socialbondnet.users.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsUsersByEmail(String email);
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "userProfile")
    Optional<Users> findById(String id);
    @EntityGraph(attributePaths = "userProfile")
    Optional<Users> findByEmail(String email);
}
