package com.example.demo.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Payment;
import com.example.demo.domain.Payment;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.util.TokenUtils;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    public Payment create(Payment payment) {
        if (payment.getUser() != null && payment.getUser().getId() != null) {
            payment.setUser(this.userService.fetchUserById(payment.getUser().getId()));
        }
        if (payment.getType().equals("DEPOSIT")) {
            String token = TokenUtils.genToken(9);
            payment.setContent(token);
        }
        return this.paymentRepository.save(payment);
    }

    public ResultPaginationDTO fetchAll(Specification<Payment> spec, Pageable pageable) {
        Page<Payment> pagePayment = this.paymentRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pagePayment.getNumber() + 1);
        mt.setPageSize(pagePayment.getSize());
        mt.setPages(pagePayment.getTotalPages());
        mt.setTotal(pagePayment.getTotalElements());

        res.setMeta(mt);
        res.setResult(pagePayment.getContent());

        return res;
    }

    public ResultPaginationDTO fetchAll(Specification<Payment> spec) {
        List<Payment> payments = this.paymentRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(payments.size());

        res.setMeta(mt);
        res.setResult(payments);

        return res;
    }

    public Payment fetchById(String id) {
        Optional<Payment> serviceOptional = this.paymentRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public void delete(String id) {
        this.paymentRepository.deleteById(id);
    }

    public Payment update(Payment updatedPayment) {
        return this.paymentRepository.save(updatedPayment);
    }

    public Payment fetchByContent(String content) {
        return this.paymentRepository.findByContent(content).orElse(null);
    }
}
