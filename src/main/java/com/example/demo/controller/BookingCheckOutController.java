package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Booking;
import com.example.demo.domain.BookingCheckOut;
import com.example.demo.domain.Service;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.BookingCheckOutService;
import com.example.demo.service.BookingService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class BookingCheckOutController {

    private final BookingCheckOutService bookingCheckOutService;
    private final BookingService bookingService;

    public BookingCheckOutController(BookingCheckOutService bookingCheckOutService, BookingService bookingService) {
        this.bookingCheckOutService = bookingCheckOutService;
        this.bookingService = bookingService;
    }

    @PostMapping("/booking/checkout")
    public ResponseEntity<BookingCheckOut> createBookingCheckOut(@Valid @RequestBody BookingCheckOut reqBookingCheckOut)
            throws Exception {
        if (reqBookingCheckOut.getBooking() != null && reqBookingCheckOut.getBooking().getId() != null) {
            Booking bookingDB = this.bookingService.fetchById(reqBookingCheckOut.getBooking().getId());
            if (LocalDate.now().isEqual(bookingDB.getDate()) && LocalTime.now().isAfter(bookingDB.getStartTime())) {
                reqBookingCheckOut.setBooking(bookingDB);
            } else if (LocalDate.now().isEqual(bookingDB.getDate())
                    && LocalTime.now().isBefore(bookingDB.getStartTime())) {
                throw new Exception("Chưa đến giờ Check-out. Vui lòng thử lại sau!");
            } else {
                throw new Exception("Check-out không thành công. Vui lòng liên hệ quản lý để biết thêm chi tiết!");
            }
        } else {
            throw new IdInvalidException("Lấy booking không thành công");
        }
        BookingCheckOut newBookingCheckOut = this.bookingCheckOutService.create(reqBookingCheckOut);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBookingCheckOut);
    }

    @GetMapping("/booking/checkout")
    public ResponseEntity<ResultPaginationDTO> fetchAllBookingCheckOuts(
            @Filter Specification<BookingCheckOut> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.bookingCheckOutService.fetchAllBookingCheckOut(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.bookingCheckOutService.fetchAllWithPagination(spec, pageable));
        }
    }

    @GetMapping("/booking/checkout/{id}")
    public ResponseEntity<BookingCheckOut> fetchBookingCheckOutById(@PathVariable("id") String id)
            throws IdInvalidException {
        BookingCheckOut service = this.bookingCheckOutService.fetchById(id);
        if (service == null) {
            throw new IdInvalidException("BookingCheckOut with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(service);
    }

    @PutMapping("/booking/checkout")
    public ResponseEntity<BookingCheckOut> updateBookingCheckOut(@Valid @RequestBody BookingCheckOut reqBookingCheckOut)
            throws IdInvalidException {
        BookingCheckOut service = this.bookingCheckOutService.fetchById(reqBookingCheckOut.getId());
        if (service == null) {
            throw new IdInvalidException("BookingCheckOut with id = " + reqBookingCheckOut.getId() + " không tồn tại");
        }
        BookingCheckOut updatedBookingCheckOut = this.bookingCheckOutService.update(reqBookingCheckOut);
        return ResponseEntity.ok(updatedBookingCheckOut);
    }

    @DeleteMapping("/booking/checkout/{id}")
    public ResponseEntity<Void> deleteBookingCheckOut(@PathVariable("id") String id) throws IdInvalidException {
        BookingCheckOut serviceDB = this.bookingCheckOutService.fetchById(id);
        if (serviceDB == null) {
            throw new IdInvalidException("BookingCheckOut with id = " + id + " không tồn tại");
        }
        this.bookingCheckOutService.delete(id);
        return ResponseEntity.ok(null);
    }

}
