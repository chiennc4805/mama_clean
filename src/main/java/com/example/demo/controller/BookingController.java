package com.example.demo.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Booking;
import com.example.demo.domain.User;
import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.BookingService;
import com.example.demo.service.UserService;
import com.example.demo.service.WalletTransactionService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final WalletTransactionService walletTransactionService;

    public BookingController(BookingService bookingService, UserService userService,
            WalletTransactionService walletTransactionService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking reqBooking)
            throws Exception {
        if (reqBooking.getCustomer() == null) {
            throw new IdInvalidException("Lịch đặt bị thiếu thông tin khách hàng");
        }
        User user = this.userService.fetchUserById(reqBooking.getCustomer().getId());
        if (user != null) {
            if (user.getBalance().compareTo(BigDecimal.valueOf(reqBooking.getTotalPrice())) >= 0) {
                user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(reqBooking.getTotalPrice())));
                this.userService.handleUpdateUser(user);
            } else {
                throw new Exception("Số dư tài khoản không đủ");
            }
        } else {
            throw new Exception("Khách hàng không tồn tại");
        }

        Booking newBooking = this.bookingService.create(reqBooking);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(BigDecimal.valueOf(reqBooking.getTotalPrice()));
        transaction.setRef_id(newBooking.getId());
        transaction.setType("BOOKING_PAYMENT");
        transaction.setUser(user);
        this.walletTransactionService.create(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @GetMapping("/bookings")
    public ResponseEntity<ResultPaginationDTO> fetchAllBookings(
            @Filter Specification<Booking> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.bookingService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size,
                    Sort.by("date").descending().by("startTime").ascending());
            return ResponseEntity.ok(this.bookingService.fetchAll(spec, pageable));
        }
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

    @PutMapping("/manual-assign-job")
    public ResponseEntity<Booking> manualAssignJob(@Valid @RequestBody Booking reqBooking)
            throws Exception {
        Booking booking = this.bookingService.fetchById(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        if (booking.getCleaner() != null) {
            throw new Exception("Công việc đã có người nhận. Thao tác thất bại.");
        }
        if (booking.getStatus() == "Đã Huỷ") {
            throw new Exception("Công việc đã bị huỷ. Thao tác thất bại.");
        }
        Booking updatedBooking = this.bookingService.update(reqBooking);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/get-available-job")
    public ResponseEntity<Booking> getAvailableJob(@Valid @RequestBody Booking reqBooking)
            throws Exception {
        Booking booking = this.bookingService.fetchById(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        if (booking.getCleaner() != null || booking.getStatus() == "Đã Huỷ") {
            throw new Exception("Công việc không còn sẵn. Thao tác thất bại.");
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
