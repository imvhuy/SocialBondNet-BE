package com.socialbondnet.users.entity;

import com.socialbondnet.users.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Users extends  BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active")
    private Boolean isActive ;

    @Column(name = "is_private")
    private Boolean isPrivate ;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Roles> roles = new HashSet<>();

//    @Enumerated(EnumType.STRING)
//    @Column(name = "account_type", length = 20)
//    private AccountType accountType = AccountType.PERSONAL;

//    @CreationTimestamp
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @Column(name = "deleted_at")
//    private LocalDateTime deletedAt;

    // Relationships
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private UserProfile userProfile;
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private PrivacySetting privacySetting;
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private NotificationPreference notificationPreference;
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private TwoFactorAuth twoFactorAuth;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<UserSession> sessions;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<OAuthProvider> oauthProviders;
//
//    // Follow relationships
//    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Follow> following;
//
//    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Follow> followers;
//
//    @Column(name = "is_verified")
//    private Boolean isVerified = false;
}
