package com.socialbondnet.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profiles extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "bio")
    private String bio = null;

    @Column(name = "gender")
    private String gender;

    @Column(name = "avatar_url")
    private String avatarUrl = null;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "location")
    private String location = null;

}
