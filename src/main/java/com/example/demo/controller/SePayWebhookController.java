package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.config.ActivityInterceptor;
import com.example.demo.domain.Payment;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.SePayPayLoad;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;
import com.example.demo.service.UserService;

@RestController
public class SePayWebhookController {

    private final PaymentService paymentService;
    private final UserService userService;

    public SePayWebhookController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @Value("${sm.sepay.access-token}")
    private String sepayAccessToken;

    @PostMapping("/webhook/sepay")
    public ResponseEntity<?> handle(@RequestHeader(value = "Authorization") String authorization,
            @RequestBody SePayPayLoad payload) {
        // Xác thực nếu bạn dùng API Key
        if (authorization == null || !authorization.equals("Apikey " +
                sepayAccessToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        // Lấy thông tin từ payload
        String transferType = payload.getTransferType(); // "in" là tiền vào

        if (!"in".equals(transferType)) {
            return ResponseEntity.ok(Map.of("success", true));
        }

        String content = payload.getContent();
        Payment payment = this.paymentService.fetchByContent(content);
        if (payment == null) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        // tránh cộng tiền 2 lần
        if ("SUCCESS".equals(payment.getStatus())) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        User user = this.userService.fetchUserByIdWithoutAuth(payment.getUser().getId());

        BigDecimal amount = BigDecimal.valueOf(payload.getTransferAmount());
        this.userService.handleUpdateBalance(user, user.getBalance().add(amount));

        payment.setStatus("SUCCESS");
        payment.setAmount(amount);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        payment.setTransactionTime(LocalDateTime.parse(payload.getTransactionDate(), formatter));
        this.paymentService.update(payment);

        return ResponseEntity.status(201).body(Map.of("success", true));
    }
}