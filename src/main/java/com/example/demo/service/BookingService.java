package com.example.demo.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Booking;
import com.example.demo.domain.User;
import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ServiceService serviceService;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, UserService userService, ServiceService serviceService,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.serviceService = serviceService;
        this.userRepository = userRepository;
    }

    public ResultPaginationDTO fetchAll(Specification<Booking> spec, Pageable pageable) {
        // String username = SecurityUtil.getCurrentUserLogin().orElse("");
        // User currentLoginUser =
        // this.userRepository.findByUsername(username).orElse(null);
        // if (currentLoginUser.getRole().getName().equals("CUSTOMER")) {
        // Specification<Booking> userSpec = (root, query, cb) ->
        // cb.equal(root.get("customer").get("id"),
        // currentLoginUser.getId());
        // spec = spec == null ? userSpec : spec.and(userSpec);
        // } else if (currentLoginUser.getRole().getName().equals("CLEANER")) {
        // Specification<Booking> userSpec = (root, query, cb) ->
        // cb.equal(root.get("cleaner").get("id"),
        // currentLoginUser.getId());
        // spec = spec == null ? userSpec : spec.and(userSpec);
        // }

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

    public List<Booking> fetchAll() {
        return this.bookingRepository.findAll();
    }

    public ResultPaginationDTO fetchAll(Specification<Booking> spec) {
        // String username = SecurityUtil.getCurrentUserLogin().orElse("");
        // User currentLoginUser =
        // this.userRepository.findByUsername(username).orElse(null);
        // if (currentLoginUser.getRole().getName().equals("CUSTOMER")) {
        // Specification<Booking> userSpec = (root, query, cb) ->
        // cb.equal(root.get("customer").get("id"),
        // currentLoginUser.getId());
        // spec = spec == null ? userSpec : spec.and(userSpec);
        // } else if (currentLoginUser.getRole().getName().equals("CLEANER")) {
        // Specification<Booking> userSpec = (root, query, cb) ->
        // cb.equal(root.get("cleaner").get("id"),
        // currentLoginUser.getId());
        // spec = spec == null ? userSpec : spec.and(userSpec);
        // }

        List<Booking> bookings = this.bookingRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(bookings.size());

        res.setMeta(mt);
        res.setResult(bookings);

        return res;
    }

    public Booking fetchById(String id) throws IdInvalidException, AccessDeniedException {
        Booking booking = this.bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            throw new IdInvalidException("Booking không tồn tại");
        }

        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER")) {
            if (!currentLoginUser.getId().equals(booking.getCustomer().getId())) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        } else if (currentLoginUser.getRole().getName().equals("CLEANER")) {
            if (!currentLoginUser.getId().equals(booking.getCleaner().getId())) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        }

        return booking;
    }

    public Booking fetchByIdWithoutAuth(String id) throws IdInvalidException {
        Booking booking = this.bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            throw new IdInvalidException("Booking không tồn tại");
        }
        return booking;
    }

    public Booking create(Booking booking) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        if (currentLoginUser.getRole().getName().equals("CLEANER")) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
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

    public Booking update(Booking updatedBooking) throws AccessDeniedException {
        if (updatedBooking.getCustomer() != null && updatedBooking.getCustomer().getId() != null) {
            updatedBooking.setCustomer(this.userService.fetchUserByIdWithoutAuth(updatedBooking.getCustomer().getId()));
        } else {
            updatedBooking.setCustomer(null);
        }

        if (updatedBooking.getCleaner() != null && updatedBooking.getCleaner().getId() != null) {
            updatedBooking.setCleaner(this.userService.fetchUserByIdWithoutAuth(updatedBooking.getCleaner().getId()));
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
