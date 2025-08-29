package org.socialbondnet.postservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostUserMentions extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String mentionedUserId;
    private String displayNameSnapshot;
    @ManyToOne
    @JoinColumn(name = "post_id" , nullable = false)
    private Posts post;
}
