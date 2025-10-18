package com.example.demo.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Booking;
import com.example.demo.domain.BookingAction;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.BookingActionRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;

@Service
public class BookingActionService {

    private final BookingActionRepository bookingActionRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final UserService userService;

    public BookingActionService(BookingActionRepository bookingActionRepository,
            BookingService bookingService, UserRepository userRepository, UserService userService) {
        this.bookingActionRepository = bookingActionRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public ResultPaginationDTO fetchAll(Specification<BookingAction> spec, Pageable pageable) {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER")) {
            Specification<BookingAction> userSpec = (root, query, cb) -> cb.equal(
                    root.get("booking").get("customer").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        } else if (currentLoginUser.getRole().getName().equals("CLEANER")) {
            Specification<BookingAction> userSpec = (root, query, cb) -> cb.equal(
                    root.get("booking").get("cleaner").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        }

        Page<BookingAction> pageBookingAction = this.bookingActionRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageBookingAction.getNumber() + 1);
        mt.setPageSize(pageBookingAction.getSize());
        mt.setPages(pageBookingAction.getTotalPages());
        mt.setTotal(pageBookingAction.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageBookingAction.getContent());

        return res;
    }

    public ResultPaginationDTO fetchAll(Specification<BookingAction> spec) {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER")) {
            Specification<BookingAction> userSpec = (root, query, cb) -> cb.equal(
                    root.get("booking").get("customer").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        } else if (currentLoginUser.getRole().getName().equals("CLEANER")) {
            Specification<BookingAction> userSpec = (root, query, cb) -> cb.equal(
                    root.get("booking").get("cleaner").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        }

        List<BookingAction> bookingActions = this.bookingActionRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(bookingActions.size());

        res.setMeta(mt);
        res.setResult(bookingActions);

        return res;
    }

    public BookingAction fetchById(String id) throws IdInvalidException {
        BookingAction bookingAction = this.bookingActionRepository.findById(id).orElse(null);
        if (bookingAction == null) {
            throw new IdInvalidException("BookingAction không tồn tại");
        }
        return bookingAction;
    }

    public BookingAction create(BookingAction bookingAction) throws AccessDeniedException, IdInvalidException {
        if (bookingAction.getBooking() != null && bookingAction.getBooking().getId() != null) {
            Booking booking = this.bookingService.fetchById(bookingAction.getBooking().getId());
            if (booking == null)
                throw new IdInvalidException("Đặt lịch không tồn tại");
            bookingAction.setBooking(booking);
        }
        if (bookingAction.getUser() != null && bookingAction.getUser().getId() != null) {
            User user = this.userService.fetchUserByIdWithoutAuth(bookingAction.getUser().getId());
            if (user == null)
                throw new IdInvalidException("Người dùng không tồn tại");
            bookingAction.setUser(user);
        }
        return this.bookingActionRepository.save(bookingAction);
    }

    public void delete(String id) {
        this.bookingActionRepository.deleteById(id);
    }

    public BookingAction update(BookingAction updatedBookingAction) throws AccessDeniedException, IdInvalidException {
        if (updatedBookingAction.getBooking() != null && updatedBookingAction.getBooking().getId() != null) {
            Booking booking = this.bookingService.fetchById(updatedBookingAction.getBooking().getId());
            if (booking == null)
                throw new IdInvalidException("Đặt lịch không tồn tại");
            updatedBookingAction.setBooking(booking);
        }
        if (updatedBookingAction.getUser() != null && updatedBookingAction.getUser().getId() != null) {
            User user = this.userService.fetchUserByIdWithoutAuth(updatedBookingAction.getUser().getId());
            if (user == null)
                throw new IdInvalidException("Người dùng không tồn tại");
            updatedBookingAction.setUser(user);
        }
        return this.bookingActionRepository.save(updatedBookingAction);
    }

}
