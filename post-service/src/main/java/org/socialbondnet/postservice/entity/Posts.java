package org.socialbondnet.postservice.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.*;
import org.socialbondnet.postservice.enums.Visibility;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "posts")
public class Posts extends  BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String title;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

}
