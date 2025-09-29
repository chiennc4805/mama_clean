package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.UserActivity;
import com.example.demo.repository.UserActivityRepository;

@Service
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    public UserActivityService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    public UserActivity create(UserActivity userActivity) {
        return this.userActivityRepository.save(userActivity);
    }

}
