package org.socialbondnet.postservice.repository;

import org.socialbondnet.postservice.entity.Timeline;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineRepository extends JpaRepository<Timeline, Long> {
    List<Timeline> findByUserIdOrderByPostCreatedAtDesc(String userId, Pageable pageable);
}
