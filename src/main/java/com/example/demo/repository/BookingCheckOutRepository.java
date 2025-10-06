package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.domain.Booking;
import com.example.demo.domain.BookingCheckOut;

public interface BookingCheckOutRepository
        extends JpaRepository<BookingCheckOut, String>, JpaSpecificationExecutor<BookingCheckOut> {

}
