package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.BookingCheckOut;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.BookingCheckOutRepository;

@Service
public class BookingCheckOutService {

    private final BookingCheckOutRepository bookingCheckOutRepository;

    public BookingCheckOutService(BookingCheckOutRepository bookingCheckOutRepository) {
        this.bookingCheckOutRepository = bookingCheckOutRepository;
    }

    public ResultPaginationDTO fetchAllBookingCheckOut(Specification<BookingCheckOut> spec) {
        List<BookingCheckOut> bookingCheckOuts = this.bookingCheckOutRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(bookingCheckOuts.size());

        res.setMeta(mt);
        res.setResult(bookingCheckOuts);

        return res;
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<BookingCheckOut> spec, Pageable pageable) {
        Page<BookingCheckOut> pageBookingCheckOut = this.bookingCheckOutRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageBookingCheckOut.getNumber() + 1);
        mt.setPageSize(pageBookingCheckOut.getSize());
        mt.setPages(pageBookingCheckOut.getTotalPages());
        mt.setTotal(pageBookingCheckOut.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageBookingCheckOut.getContent());

        return res;
    }

    public BookingCheckOut fetchById(String id) {
        Optional<BookingCheckOut> bookingCheckOutOptional = this.bookingCheckOutRepository.findById(id);
        return bookingCheckOutOptional.isPresent() ? bookingCheckOutOptional.get() : null;
    }

    public BookingCheckOut fetchByBookingId(String bookingId) {
        Optional<BookingCheckOut> bookingCheckOutOptional = this.bookingCheckOutRepository.findByBookingId(bookingId);
        return bookingCheckOutOptional.isPresent() ? bookingCheckOutOptional.get() : null;
    }

    public BookingCheckOut create(BookingCheckOut bookingCheckOut) {
        return this.bookingCheckOutRepository.save(bookingCheckOut);
    }

    public void delete(String id) {
        this.bookingCheckOutRepository.deleteById(id);
    }

    public BookingCheckOut update(BookingCheckOut updatedBookingCheckOut) {
        return this.bookingCheckOutRepository.save(updatedBookingCheckOut);
    }

}
