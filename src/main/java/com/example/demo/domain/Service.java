package com.example.demo.domain;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "services")
public class Service {

    @Id
    @UuidGenerator
    private String id;

    private String name;
    private int duration; // unit: hour

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private double area;
    private double price;

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    private List<Booking> bookings;
}
