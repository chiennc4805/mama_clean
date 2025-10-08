package com.example.demo.domain;

import java.time.LocalDate;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cleaner_profiles")
public class CleanerProfile {

    @Id
    @UuidGenerator
    private String id;

    private LocalDate dob;
    private double rating = 5;
    private int ratingCount = 1;
    private double experience; // unit: days (maybe)
    private boolean available;
    private String area;
    private String bankNo;
    private String bank;
    private String idNumber;
    private LocalDate idDate;
    private String idPlace;
    private String address;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "userActivities", "refreshToken" })
    private User user;

}
