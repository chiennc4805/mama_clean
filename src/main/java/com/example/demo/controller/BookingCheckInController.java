package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Booking;
import com.example.demo.domain.BookingCheckIn;
import com.example.demo.domain.BookingCheckOut;
import com.example.demo.domain.Feedback;
import com.example.demo.domain.dto.request.reqCheckIn;
import com.example.demo.service.BookingCheckInService;
import com.example.demo.service.BookingService;
import com.example.demo.util.GeoUtils;
import com.example.demo.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BookingCheckInController {

    private final BookingCheckInService bookingCheckInService;
    private final BookingService bookingService;

    public BookingCheckInController(BookingCheckInService bookingCheckInService, BookingService bookingService) {
        this.bookingCheckInService = bookingCheckInService;
        this.bookingService = bookingService;
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

    @GetMapping("/booking/checkin/{id}")
    public ResponseEntity<BookingCheckIn> fetchCheckInByBookingId(@PathVariable("id") String bookingId)
            throws Exception {
        Booking booking = this.bookingService.fetchById(bookingId);
        if (booking == null) {
            throw new Exception("Đặt lịch không tồn tại");
        }
        BookingCheckIn bookingCheckIn = this.bookingCheckInService.fetchByBookingId(bookingId);
        if (bookingCheckIn == null) {
            throw new IdInvalidException("Thông tin check-in không tồn tại");
        }
        return ResponseEntity.ok(bookingCheckIn);
    }

    @PostMapping("/booking/checkin")
    public ResponseEntity<BookingCheckIn> createBooking(@Valid @RequestBody BookingCheckIn reqBookingCheckIn)
            throws Exception {
        if (reqBookingCheckIn.getBooking() != null && reqBookingCheckIn.getBooking().getId() != null) {
            Booking bookingDB = this.bookingService.fetchById(reqBookingCheckIn.getBooking().getId());
            if (LocalDate.now().isEqual(bookingDB.getDate())) {
                reqBookingCheckIn.setBooking(bookingDB);
            } else {
                throw new Exception("Check-in không thành công. Vui lòng liên hệ quản lý để biết thêm chi tiết!");
            }
        } else {
            throw new IdInvalidException("Lấy booking không thành công");
        }
        BookingCheckIn newBooking = this.bookingCheckInService.create(reqBookingCheckIn);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);

        // }else
        // if(LocalDate.now().isEqual(bookingDB.getDate())&&LocalTime.now().isAfter(bookingDB.getStartTime()))
        // throw new Exception("Đã quá giờ. Check-in thất bại. Vui lòng liên hệ quản
        // lý!");
    }

    @DeleteMapping("/booking/checkin/{id}")
    public ResponseEntity<Void> deleteBookingCheckOut(@PathVariable("id") String id) throws IdInvalidException {
        BookingCheckIn serviceDB = this.bookingCheckInService.fetchById(id);
        if (serviceDB == null) {
            throw new IdInvalidException("BookingCheckIn with id = " + id + " không tồn tại");
        }
        this.bookingCheckInService.delete(id);
        return ResponseEntity.ok(null);
    }
}
