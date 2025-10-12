package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.domain.BookingCheckIn;
import com.example.demo.domain.BookingCheckOut;
import com.example.demo.repository.BookingCheckInRepository;

@Service
public class BookingCheckInService {

    private final BookingCheckInRepository bookingCheckInRepository;
    private final BookingService bookingService;

    public BookingCheckInService(BookingCheckInRepository bookingCheckInRepository, BookingService bookingService) {
        this.bookingCheckInRepository = bookingCheckInRepository;
        this.bookingService = bookingService;
    }

    public BookingCheckIn create(BookingCheckIn bookingCheckIn) {
        if (bookingCheckIn.getBooking().getId() != null) {
            bookingCheckIn.setBooking(this.bookingService.fetchById(bookingCheckIn.getBooking().getId()));
        }
        return this.bookingCheckInRepository.save(bookingCheckIn);
    }

    public BookingCheckIn fetchById(String id) {
        Optional<BookingCheckIn> bookingCheckOutOptional = this.bookingCheckInRepository.findById(id);
        return bookingCheckOutOptional.isPresent() ? bookingCheckOutOptional.get() : null;
    }

    public BookingCheckIn fetchByBookingId(String bookingId) {
        Optional<BookingCheckIn> bookingCheckOutOptional = this.bookingCheckInRepository.findByBookingId(bookingId);
        return bookingCheckOutOptional.isPresent() ? bookingCheckOutOptional.get() : null;
    }

    public void delete(String id) {
        this.bookingCheckInRepository.deleteById(id);
    }

}
