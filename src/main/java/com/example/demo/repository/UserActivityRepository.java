package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.domain.UserActivity;

public interface UserActivityRepository
        extends JpaRepository<UserActivity, String>, JpaSpecificationExecutor<UserActivity> {

}
