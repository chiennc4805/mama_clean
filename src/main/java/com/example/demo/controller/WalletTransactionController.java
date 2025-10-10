package com.example.demo.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.WalletTransactionService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<WalletTransaction> createWalletTransaction(
            @Valid @RequestBody WalletTransaction reqWalletTransaction)
            throws IdInvalidException {
        WalletTransaction newWalletTransaction = this.walletTransactionService.create(reqWalletTransaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(newWalletTransaction);
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
            Pageable pageable = PageRequest.of(page - 1, size);
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
            throws IdInvalidException {
        WalletTransaction walletTransaction = this.walletTransactionService.fetchById(reqWalletTransaction.getId());
        if (walletTransaction == null) {
            throw new IdInvalidException(
                    "WalletTransaction with id = " + reqWalletTransaction.getId() + " không tồn tại");
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
