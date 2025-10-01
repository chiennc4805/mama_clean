package com.example.demo.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    private String id;
    private String username;
    private String password;

    private String name;
    private String phone;
    private String email;
    private boolean gender;
    private double balance = 0;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne()
    @JoinColumn(name = "role_id")
    private Role role;

    private boolean status = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String profileImage;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Booking> bookings;

    @OneToMany(mappedBy = "cleaner")
    @JsonIgnore
    private List<Booking> workings;

    // feedback ??

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<UserAddress> userAddresses;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    // @JsonIgnoreProperties("user")
    private List<UserActivity> userActivities;

}

// @OneToOne(mappedBy = "user", orphanRemoval = true, cascade =
// CascadeType.REMOVE)
// @JsonIgnoreProperties({ "user", "subjects" })
// private Teacher teacherInfo;

// @OneToOne(mappedBy = "user", orphanRemoval = true, cascade =
// CascadeType.REMOVE)
// @JsonIgnoreProperties({ "user", "subjects" })
// private Parent parentInfo;
