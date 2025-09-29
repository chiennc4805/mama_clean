package com.example.demo.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pending_users")
public class PendingUser {

    @Id
    @UuidGenerator
    private String id;
    private String password;

    private String name;
    private String phone;
    private String email;
    private boolean gender;

    @ManyToOne()
    @JoinColumn(name = "role_id")
    private Role role;

    private String profileImage;

    private String otpCode;
    private LocalDateTime otpRequestedTime;

}
