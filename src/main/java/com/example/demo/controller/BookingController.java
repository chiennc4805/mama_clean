package com.example.demo.controller;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Booking;
import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.User;
import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import com.example.demo.service.CleanerProfileService;
import com.example.demo.service.UserService;
import com.example.demo.service.WalletTransactionService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final WalletTransactionService walletTransactionService;
    private final CleanerProfileService cleanerProfileService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserService userService, UserRepository userRepository,
            WalletTransactionService walletTransactionService, CleanerProfileService cleanerProfileService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.walletTransactionService = walletTransactionService;
        this.cleanerProfileService = cleanerProfileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking reqBooking)
            throws AccessDeniedException, IdInvalidException {
        if (reqBooking.getCustomer() == null) {
            throw new IdInvalidException("Lịch đặt bị thiếu thông tin khách hàng");
        }
        User customer = this.userService.fetchUserById(reqBooking.getCustomer().getId());
        if (customer == null) {
            throw new IdInvalidException("Khách hàng không tồn tại");
        }

        reqBooking.setCustomer(customer);
        Booking newBooking = this.bookingService.create(reqBooking);

        if (customer.getBalance().compareTo(BigDecimal.valueOf(reqBooking.getTotalPrice())) >= 0) {
            this.userService.handleUpdateBalance(customer,
                    customer.getBalance().subtract(BigDecimal.valueOf(reqBooking.getTotalPrice())));
        } else {
            throw new IdInvalidException("Số dư tài khoản không đủ");
        }

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(BigDecimal.valueOf(reqBooking.getTotalPrice()));
        transaction.setRef_id(newBooking.getId());
        transaction.setType("BOOKING_PAYMENT");
        transaction.setStatus("SUCCESS");
        transaction.setUser(customer);
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
    public ResponseEntity<Booking> fetchBookingById(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        Booking booking = this.bookingService.fetchById(id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/bookings")
    public ResponseEntity<Booking> updateBooking(@Valid @RequestBody Booking reqBooking)
            throws IdInvalidException, AccessDeniedException {
        // authenticate
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        Booking booking = this.bookingService.fetchById(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        if (reqBooking.getStatus().equals("Đã hoàn thành") && booking.getStatus().equals("Chờ Check-out")) {
            if (!currentLoginUser.getRole().getName().equals("CLEANER")) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
            if (reqBooking.getCleaner() == null) {
                throw new IdInvalidException("Lịch đặt không có người dọn dẹp");
            }
            User cleaner = this.userService.fetchUserById(reqBooking.getCleaner().getId());
            if (cleaner == null) {
                throw new IdInvalidException("Người dọn dẹp không tồn tại");
            }
            CleanerProfile cleanerProfile = this.cleanerProfileService.fetchByUserId(cleaner.getId());
            BigDecimal income = BigDecimal.valueOf(Math.round(reqBooking.getTotalPrice() * 0.8));
            cleaner.setBalance(income);
            cleanerProfile.setTotalIncome(cleanerProfile.getTotalIncome().add(income));
            this.userService.handleUpdateUser(cleaner);
            this.cleanerProfileService.update(cleanerProfile);
        } else if (reqBooking.getStatus().equals("Đã huỷ") && !booking.getStatus().equals("Đã huỷ")) {
            if (!currentLoginUser.getRole().getName().equals("CUSTOMER")) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
            // refund for customer when cancel booking
            User customer = this.userService.fetchUserById(reqBooking.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Khách hàng không tồn tại");
            }
            customer.setBalance(customer.getBalance().add(BigDecimal.valueOf(reqBooking.getTotalPrice())));
            this.userService.handleUpdateUser(customer);

            // create transaction type refund
            WalletTransaction transaction = new WalletTransaction();
            transaction.setAmount(BigDecimal.valueOf(reqBooking.getTotalPrice()));
            transaction.setRef_id(reqBooking.getId());
            transaction.setType("REFUND");
            transaction.setStatus("SUCCESS");
            transaction.setUser(customer);
            this.walletTransactionService.create(transaction);
        }
        Booking updatedBooking = this.bookingService.update(reqBooking);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/manual-assign-job")
    public ResponseEntity<Booking> manualAssignJob(@Valid @RequestBody Booking reqBooking)
            throws Exception {
        Booking booking = this.bookingService.fetchByIdWithoutAuth(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        if (booking.getCleaner() != null) {
            throw new Exception("Công việc đã có người nhận. Thao tác thất bại.");
        }
        if (booking.getStatus().equals("Đã huỷ")) {
            throw new Exception("Công việc đã bị huỷ. Thao tác thất bại.");
        }
        Booking updatedBooking = this.bookingService.update(reqBooking);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/get-available-job")
    public ResponseEntity<Booking> getAvailableJob(@Valid @RequestBody Booking reqBooking)
            throws Exception {
        Booking booking = this.bookingService.fetchByIdWithoutAuth(reqBooking.getId());
        if (booking == null) {
            throw new IdInvalidException("Booking with id = " + reqBooking.getId() + " không tồn tại");
        }
        if (booking.getCleaner() != null || booking.getStatus().equals("Đã huỷ")) {
            throw new Exception("Công việc không còn sẵn. Thao tác thất bại.");
        }
        Booking updatedBooking = this.bookingService.update(reqBooking);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/get-all-booking-income")
    public ResponseEntity<Double> getMethodName() {
        List<Booking> bookings = this.bookingService.fetchAll();
        Double allBookingIncome = bookings.stream().mapToDouble(Booking::getTotalPrice)
                .sum();
        return ResponseEntity.ok(allBookingIncome);
    }

    // @DeleteMapping("/bookings/{id}")
    // public ResponseEntity<Void> deleteBooking(@PathVariable("id") String id)
    // throws IdInvalidException {
    // Booking bookingDB = this.bookingService.fetchById(id);
    // if (bookingDB == null) {
    // throw new IdInvalidException("Booking with id = " + id + " không tồn tại");
    // }
    // this.bookingService.delete(id);
    // return ResponseEntity.ok(null);
    // }

}
