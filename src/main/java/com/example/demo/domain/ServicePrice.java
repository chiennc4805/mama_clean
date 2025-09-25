package com.example.demo.domain;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "service_prices")
public class ServicePrice {

    @Id
    @UuidGenerator
    private String id;

    private double price;

    @ManyToOne
    @JoinColumn(name = "duration_id")
    private Duration duration;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @OneToMany(mappedBy = "servicePrice")
    @JsonIgnore
    private List<Booking> bookings;
}
