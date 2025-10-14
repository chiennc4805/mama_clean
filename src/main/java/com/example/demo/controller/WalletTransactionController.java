package com.example.demo.controller;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

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

import com.example.demo.domain.User;
import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.UserService;
import com.example.demo.service.WalletTransactionService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;
    private final UserService userService;

    public WalletTransactionController(WalletTransactionService walletTransactionService, UserService userService) {
        this.walletTransactionService = walletTransactionService;
        this.userService = userService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<WalletTransaction> createWalletTransaction(
            @Valid @RequestBody WalletTransaction reqWalletTransaction)
            throws Exception {
        // cleaner rút tiền
        if (reqWalletTransaction.getType().equals("WITHDRAW") && reqWalletTransaction.getStatus().equals("PENDING")) {
            if (reqWalletTransaction.getUser() == null) {
                throw new Exception("Giao dịch không có người tạo");
            }
            User user = this.userService.fetchUserById(reqWalletTransaction.getUser().getId());
            if (user == null) {
                throw new IdInvalidException("Người dùng không tồn tại");
            }
            if (!user.getRole().getName().equals("CLEANER")) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
            if (user.getBalance().compareTo(reqWalletTransaction.getAmount()) == -1) {
                throw new Exception("Số dư không đủ");
            }
            // user.setBalance(user.getBalance().subtract(reqWalletTransaction.getAmount()));
            // this.userService.handleUpdateUser(user);
            WalletTransaction newWalletTransaction = this.walletTransactionService.create(reqWalletTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(newWalletTransaction);
        }
        throw new Exception("Giá trị không hợp lệ");

    }

    @GetMapping("/transactions")
    public ResponseEntity<ResultPaginationDTO> fetchAllWalletTransactions(
            @Filter Specification<WalletTransaction> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.walletTransactionService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(this.walletTransactionService.fetchAll(spec, pageable));
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<WalletTransaction> fetchWalletTransactionById(@PathVariable("id") String id)
            throws IdInvalidException {
        WalletTransaction walletTransaction = this.walletTransactionService.fetchById(id);
        if (walletTransaction == null) {
            throw new IdInvalidException("WalletTransaction with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(walletTransaction);
    }

    @PutMapping("/transactions")
    public ResponseEntity<WalletTransaction> updateWalletTransaction(
            @Valid @RequestBody WalletTransaction reqWalletTransaction)
            throws Exception {
        WalletTransaction walletTransaction = this.walletTransactionService.fetchById(reqWalletTransaction.getId());
        if (walletTransaction == null) {
            throw new IdInvalidException(
                    "WalletTransaction with id = " + reqWalletTransaction.getId() + " không tồn tại");
        }
        if (reqWalletTransaction.getType().equals("WITHDRAW")) {
            if (walletTransaction.getStatus().equals("PENDING") && reqWalletTransaction.getStatus().equals("SUCCESS")) {
                if (reqWalletTransaction.getUser() == null) {
                    throw new Exception("Giao dịch không có người tạo");
                }
                User user = this.userService.fetchUserById(reqWalletTransaction.getUser().getId());
                if (user == null) {
                    throw new Exception("Người dùng không tồn tại");
                }
                if (user.getBalance().compareTo(reqWalletTransaction.getAmount()) == -1) {
                    throw new Exception("Số dư không đủ");
                }
                this.userService.handleUpdateBalance(user,
                        user.getBalance().subtract(reqWalletTransaction.getAmount()));
            }
        }
        WalletTransaction updatedWalletTransaction = this.walletTransactionService.update(reqWalletTransaction);
        return ResponseEntity.ok(updatedWalletTransaction);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteWalletTransaction(@PathVariable("id") String id) throws IdInvalidException {
        WalletTransaction walletTransactionDB = this.walletTransactionService.fetchById(id);
        if (walletTransactionDB == null) {
            throw new IdInvalidException("WalletTransaction with id = " + id + " không tồn tại");
        }
        this.walletTransactionService.delete(id);
        return ResponseEntity.ok(null);
    }
}
