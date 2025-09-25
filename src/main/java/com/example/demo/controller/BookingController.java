package com.example.demo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.domain.Booking;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.BookingService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@Controller
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking reqBooking)
            throws IdInvalidException {
        Booking newBooking = this.bookingService.create(reqBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @GetMapping("/bookings")
    public ResponseEntity<ResultPaginationDTO> fetchAllBookings(
            @Filter Specification<Booking> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.bookingService.fetchAll(spec, pageable));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> fetchBookingById(@PathVariable("id") String id) throws IdInvalidException {
        Booking booking = this.bookingService.fetchById(id);
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/bookings")
    public ResponseEntity<Booking> updateBooking(@Valid @RequestBody Booking reqBooking)
            throws IdInvalidException {
        Booking booking = this.bookingService.fetchById(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        Booking updatedBooking = this.bookingService.update(reqBooking);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") String id) throws IdInvalidException {
        Booking bookingDB = this.bookingService.fetchById(id);
        if (bookingDB == null) {
            throw new IdInvalidException("Booking with id = " + id + " không tồn tại");
        }
        this.bookingService.delete(id);
        return ResponseEntity.ok(null);
    }

}
