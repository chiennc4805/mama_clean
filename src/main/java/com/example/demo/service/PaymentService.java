package com.example.demo.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
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
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.TokenUtils;
import com.example.demo.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserService userService, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Payment create(Payment payment) throws AccessDeniedException {
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
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER") || currentLoginUser.getRole().getName()
                .equals("CLEANER")) {
            Specification<Payment> userSpec = (root, query, cb) -> cb.equal(root.get("user").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        }

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
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER") || currentLoginUser.getRole().getName()
                .equals("CLEANER")) {
            Specification<Payment> userSpec = (root, query, cb) -> cb.equal(root.get("user").get("id"),
                    currentLoginUser.getId());
            spec = spec == null ? userSpec : spec.and(userSpec);
        }

        List<Payment> payments = this.paymentRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(payments.size());

        res.setMeta(mt);
        res.setResult(payments);

        return res;
    }

    public Payment fetchById(String id) throws IdInvalidException, AccessDeniedException {
        Payment payment = this.paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            throw new IdInvalidException("Thanh toán không tồn tại");
        }

        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER")
                || currentLoginUser.getRole().getName().equals("CLEANER")) {
            if (!payment.getUser().getId().equals(currentLoginUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        }

        return payment;
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
