package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.domain.BookingCheckIn;

public interface BookingCheckInRepository
        extends JpaRepository<BookingCheckIn, String>, JpaSpecificationExecutor<BookingCheckIn> {

    Optional<BookingCheckIn> findByBookingId(String bookingId);

}
