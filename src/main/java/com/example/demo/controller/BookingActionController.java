package com.example.demo.controller;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.domain.BookingAction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.BookingActionService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BookingActionController {

    private final BookingActionService bookingActionService;

    public BookingActionController(BookingActionService bookingActionActionService) {
        this.bookingActionService = bookingActionActionService;
    }

    @PostMapping("/booking-actions")
    public ResponseEntity<BookingAction> createBookingAction(@Valid @RequestBody BookingAction reqBookingAction)
            throws AccessDeniedException, IdInvalidException {
        BookingAction newBookingAction = this.bookingActionService.create(reqBookingAction);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBookingAction);
    }

    @GetMapping("/booking-actions")
    public ResponseEntity<ResultPaginationDTO> fetchAllBookingActions(
            @Filter Specification<BookingAction> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.bookingActionService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.bookingActionService.fetchAll(spec, pageable));
        }
    }

    @GetMapping("/booking-actions/{id}")
    public ResponseEntity<BookingAction> fetchBookingActionById(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        BookingAction bookingAction = this.bookingActionService.fetchById(id);
        return ResponseEntity.ok(bookingAction);
    }

    @PutMapping("/booking-actions")
    public ResponseEntity<BookingAction> updateBookingAction(@Valid @RequestBody BookingAction reqBookingAction)
            throws IdInvalidException, AccessDeniedException {
        BookingAction updatedBookingAction = this.bookingActionService.update(reqBookingAction);
        return ResponseEntity.ok(updatedBookingAction);
    }

}
