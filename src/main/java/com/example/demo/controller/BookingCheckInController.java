package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.domain.BookingCheckIn;
import com.example.demo.domain.dto.request.reqCheckIn;
import com.example.demo.service.BookingCheckInService;
import com.example.demo.util.GeoUtils;
import com.example.demo.util.error.IdInvalidException;

import jakarta.validation.Valid;

@Controller
public class BookingCheckInController {

    private final BookingCheckInService bookingCheckInService;

    public BookingCheckInController(BookingCheckInService bookingCheckInService) {
        this.bookingCheckInService = bookingCheckInService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<String> cleanerCheckIn(@RequestBody reqCheckIn reqCheckIn) {
        double distance = GeoUtils.distance(reqCheckIn.getCustomerLat(), reqCheckIn.getCustomerLon(),
                reqCheckIn.getCleanerLat(), reqCheckIn.getCleanerLon());

        String msg = "";
        if (distance > 100000) {
            msg = "Khoảng cách quá xa";
        } else {
            msg = "Thành công";
        }
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/booking-check-in")
    public ResponseEntity<BookingCheckIn> createBooking(@Valid @RequestBody BookingCheckIn bookingCheckIn)
            throws IdInvalidException {
        BookingCheckIn newBooking = this.bookingCheckInService.create(bookingCheckIn);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }
}
