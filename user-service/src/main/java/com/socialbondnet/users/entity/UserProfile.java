package com.socialbondnet.users.entity;

import com.socialbondnet.users.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity{
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;                // trùng với users.id

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    private Users user;

    @Column(length = 80)
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "bio")
    private String bio = null;

    @Column(length = 120)
    private String website;

    @Column(name = "location")
    private String location = null;

    @Column(name = "avatar_url")
    private String avatarUrl ;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(length = 512)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Visibility visibility = Visibility.PUBLIC;

}
