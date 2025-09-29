package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.domain.PendingUser;

public interface PendingUserRepository
        extends JpaRepository<PendingUser, String>, JpaSpecificationExecutor<PendingUser> {

    Optional<PendingUser> findByEmail(String email);

}
