package org.socialbondnet.postservice.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.*;
import org.socialbondnet.postservice.enums.Visibility;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts")
public class Posts extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String title;
    private String imageUrl;
    private String displayNameSnapshot;
    private String avatarUrlSnapshot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostUserMentions> mentions;
}
