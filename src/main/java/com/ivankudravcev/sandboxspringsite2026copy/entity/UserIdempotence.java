package com.ivankudravcev.sandboxspringsite2026copy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Entity
@Table(name = "t_user_idempotence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdempotence {
    @Id
    @Column(name = "c_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "c_id")
    @JsonBackReference
    private User user;
    @Column(name = "c_idempotence_key")
    private String idempotenceKey;
    @Column(name = "c_created_at")
    private LocalDateTime createdAt;
    @Column(name = "c_product")
    private String product;
    @Column(name = "c_payment_id")
    private String paymentId;
}
