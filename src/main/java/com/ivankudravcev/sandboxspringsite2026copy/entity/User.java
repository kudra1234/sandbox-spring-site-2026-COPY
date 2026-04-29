package com.ivankudravcev.sandboxspringsite2026copy.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "c_username", nullable = false, unique = true)
    private String username;

    @Column(name = "c_email" , nullable = false, unique = true)
    private String email;

    @Column(name = "c_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "c_refresh_token")
    private String refreshToken;

    @Column(name = "c_google_id")
    private String googleId;

    @Column(name = "c_email_confirmed", nullable = false)
    private boolean emailConfirmed = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private UserPassword password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<UserRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<UserPurchases> purchases = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private UserIdempotence idempotence;


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", googleId='" + googleId + '\'' +
                // НЕ включаем связанные сущности!
                '}';
    }


}

