package com.ivankudravcev.sandboxspringsite2026copy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;


@Entity
@Table(name = "t_user_password")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "c_id", referencedColumnName = "id", unique = true)
    @JsonBackReference
    private User user;

    @Column(name = "c_password")
    private String password;



}
