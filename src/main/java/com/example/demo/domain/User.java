package com.example.demo.domain;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

    @Column(precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.valueOf(0);

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

    private String avatar = "default.webp";

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Booking> bookings;

    @OneToMany(mappedBy = "cleaner")
    @JsonIgnore
    private List<Booking> workings;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<UserAddress> userAddresses;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<UserActivity> userActivities;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private CleanerProfile cleanerProfile;

    @Formula("(SELECT ua.request_time FROM user_activities ua WHERE ua.user_id = id ORDER BY ua.request_time DESC LIMIT 1)")
    private LocalDateTime latestActivityTime;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Payment> payments;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<WalletTransaction> walletTransactions;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<BookingAction> bookingActions;

}

// @OneToOne(mappedBy = "user", orphanRemoval = true, cascade =
// CascadeType.REMOVE)
// @JsonIgnoreProperties({ "user", "subjects" })
// private Teacher teacherInfo;

// @OneToOne(mappedBy = "user", orphanRemoval = true, cascade =
// CascadeType.REMOVE)
// @JsonIgnoreProperties({ "user", "subjects" })
// private Parent parentInfo;
