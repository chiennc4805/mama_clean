package com.example.demo.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.example.demo.domain.Payment;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.PaymentService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment reqPayment)
            throws IdInvalidException {
        Payment newPayment = this.paymentService.create(reqPayment);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPayment);
    }

    @GetMapping("/payments")
    public ResponseEntity<ResultPaginationDTO> fetchAllPayments(
            @Filter Specification<Payment> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.paymentService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.paymentService.fetchAll(spec, pageable));
        }
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> fetchPaymentById(@PathVariable("id") String id) throws IdInvalidException {
        Payment payment = this.paymentService.fetchById(id);
        if (payment == null) {
            throw new IdInvalidException("Payment with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/payments")
    public ResponseEntity<Payment> updatePayment(@Valid @RequestBody Payment reqPayment)
            throws IdInvalidException {
        Payment payment = this.paymentService.fetchById(reqPayment.getId());
        if (payment == null) {
            throw new IdInvalidException("Payment with id = " + reqPayment.getId() + " không tồn tại");
        }
        Payment updatedPayment = this.paymentService.update(reqPayment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable("id") String id) throws IdInvalidException {
        Payment paymentDB = this.paymentService.fetchById(id);
        if (paymentDB == null) {
            throw new IdInvalidException("Payment with id = " + id + " không tồn tại");
        }
        this.paymentService.delete(id);
        return ResponseEntity.ok(null);
    }

}
