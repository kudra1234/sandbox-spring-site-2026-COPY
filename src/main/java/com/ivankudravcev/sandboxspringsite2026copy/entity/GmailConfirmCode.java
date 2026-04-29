package com.ivankudravcev.sandboxspringsite2026copy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gmail_confirm_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailConfirmCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "c_mail_code", nullable = false, unique = true)
    private String mailCode;

    @CreationTimestamp
    @Column(name = "c_created_at")
    private LocalDateTime createdAt;
}