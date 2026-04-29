package com.ivankudravcev.sandboxspringsite2026copy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Entity
@Table(name = "t_user_purchases")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPurchases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "c_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @Column(name = "c_name", nullable = false)
    private String name;
    @Column(name = "c_price", nullable = false)
    private String price;
    @Column(name = "c_expires_at")
    private Instant expiresAt;
    @Column(name = "c_purchase_at")
    private Instant purchaseAt;

}
