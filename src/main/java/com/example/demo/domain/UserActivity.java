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
@Table(name = "user_activities")
public class UserActivity {

    @Id
    @UuidGenerator
    private String id;
    private String action;
    private LocalDateTime requestTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
