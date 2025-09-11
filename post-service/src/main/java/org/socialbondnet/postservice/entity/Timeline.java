package org.socialbondnet.postservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socialbondnet.postservice.enums.Visibility;

import java.time.LocalDateTime;

@Entity
@Table(name = "timeline")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timeline extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String postId;

    @Column(nullable = false)
    private String postAuthorId;

    private String postTitle;

    private String postImageUrl;

    private Visibility postVisibility;

    private String authorDisplayName;

    private String authorAvatarUrl;

    private LocalDateTime postCreatedAt;

}