package com.example.demo.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.User;
import com.example.demo.domain.WalletTransaction;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTransactionRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.TokenUtils;

@Service
public class WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository, UserService userService,
            UserRepository userRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public WalletTransaction create(WalletTransaction walletTransaction) throws AccessDeniedException {
        if (walletTransaction.getUser() != null && walletTransaction.getUser().getId() != null) {
            walletTransaction.setUser(this.userService.fetchUserById(walletTransaction.getUser().getId()));
        }
        return this.walletTransactionRepository.save(walletTransaction);
    }

    public ResultPaginationDTO fetchAll(Specification<WalletTransaction> spec, Pageable pageable) {
        Page<WalletTransaction> pageWalletTransaction = this.walletTransactionRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageWalletTransaction.getNumber() + 1);
        mt.setPageSize(pageWalletTransaction.getSize());
        mt.setPages(pageWalletTransaction.getTotalPages());
        mt.setTotal(pageWalletTransaction.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageWalletTransaction.getContent());

        return res;
    }

    public ResultPaginationDTO fetchAll(Specification<WalletTransaction> spec) {
        List<WalletTransaction> walletTransactions = this.walletTransactionRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(walletTransactions.size());

        res.setMeta(mt);
        res.setResult(walletTransactions);

        return res;
    }

    public WalletTransaction fetchById(String id) {
        Optional<WalletTransaction> serviceOptional = this.walletTransactionRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public void delete(String id) {
        this.walletTransactionRepository.deleteById(id);
    }

    public WalletTransaction update(WalletTransaction updatedWalletTransaction) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (!currentLoginUser.getRole().getName().equals("SUPER_ADMIN")) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }

        if (updatedWalletTransaction.getUser() != null && updatedWalletTransaction.getUser().getId() != null) {
            updatedWalletTransaction.setUser(this.userService.fetchUserById(
                    updatedWalletTransaction.getUser().getId()));
        }
        return this.walletTransactionRepository.save(updatedWalletTransaction);
    }
}
