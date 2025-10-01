package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.CleanerProfile;

@Repository
public interface CleanerProfileRepository
                extends JpaRepository<CleanerProfile, String>, JpaSpecificationExecutor<CleanerProfile> {

        Optional<CleanerProfile> findByUserId(String userId);

}
