package org.socialbondnet.postservice.repository;

import org.socialbondnet.postservice.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Posts, String> {
}
