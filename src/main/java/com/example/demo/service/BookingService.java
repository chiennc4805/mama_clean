package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Booking;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ServiceService serviceService;

    public BookingService(BookingRepository bookingRepository, UserService userService, ServiceService serviceService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.serviceService = serviceService;
    }

    public ResultPaginationDTO fetchAll(Specification<Booking> spec, Pageable pageable) {
        Page<Booking> pageBooking = this.bookingRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageBooking.getNumber() + 1);
        mt.setPageSize(pageBooking.getSize());
        mt.setPages(pageBooking.getTotalPages());
        mt.setTotal(pageBooking.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageBooking.getContent());

        return res;
    }

    public ResultPaginationDTO fetchAll(Specification<Booking> spec) {
        List<Booking> bookings = this.bookingRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(bookings.size());

        res.setMeta(mt);
        res.setResult(bookings);

        return res;
    }

    public Booking fetchById(String id) {
        Optional<Booking> serviceOptional = this.bookingRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public Booking create(Booking booking) {
        if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
            booking.setCustomer(this.userService.fetchUserById(booking.getCustomer().getId()));
        } else {
            booking.setCustomer(null);
        }

        if (booking.getCleaner() != null && booking.getCleaner().getId() != null) {
            booking.setCleaner(this.userService.fetchUserById(booking.getCleaner().getId()));
        } else {
            booking.setCleaner(null);
        }

        if (booking.getService().getId() != null) {
            booking.setService(this.serviceService.fetchById(booking.getService().getId()));
        } else {
            booking.setService(null);
        }
        return this.bookingRepository.save(booking);
    }

    public void delete(String id) {
        this.bookingRepository.deleteById(id);
    }

    public Booking update(Booking updatedBooking) {
        if (updatedBooking.getCustomer() != null && updatedBooking.getCustomer().getId() != null) {
            updatedBooking.setCustomer(this.userService.fetchUserById(updatedBooking.getCustomer().getId()));
        } else {
            updatedBooking.setCustomer(null);
        }

        if (updatedBooking.getCleaner() != null && updatedBooking.getCleaner().getId() != null) {
            updatedBooking.setCleaner(this.userService.fetchUserById(updatedBooking.getCleaner().getId()));
        } else {
            updatedBooking.setCleaner(null);
        }

        if (updatedBooking.getService().getId() != null) {
            updatedBooking.setService(this.serviceService.fetchById(updatedBooking.getService().getId()));
        } else {
            updatedBooking.setService(null);
        }

        return this.bookingRepository.save(updatedBooking);
    }
}
