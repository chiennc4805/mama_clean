package com.example.demo.service;

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

    public Booking fetchById(String id) {
        Optional<Booking> serviceOptional = this.bookingRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public Booking create(Booking booking) {
        if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
            booking.setCustomer(this.userService.fetchUserById(booking.getCustomer().getId()));
        }
        if (booking.getCleaner() != null && booking.getCleaner().getId() != null) {
            booking.setCleaner(this.userService.fetchUserById(booking.getCleaner().getId()));
        }
        if (booking.getService().getId() != null) {
            booking.setService(this.serviceService.fetchById(booking.getService().getId()));
        }
        return this.bookingRepository.save(booking);
    }

    public void delete(String id) {
        this.bookingRepository.deleteById(id);
    }

    public Booking update(Booking updatedBooking) {
        return this.bookingRepository.save(updatedBooking);
    }
}
