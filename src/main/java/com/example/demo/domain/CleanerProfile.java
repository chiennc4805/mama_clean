package com.example.demo.domain;

import org.hibernate.annotations.UuidGenerator;

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

    private double rating;
    private double experience; // unit: days (maybe)
    private boolean available;
    private String area;
    private String bankNo;
    private String bank;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
