package com.example.demo.service;

import java.util.Optional;

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

    public UserActivity getLatestActivityByUserId(String userId) {
        UserActivity latestActivity = userActivityRepository.findTopByUserIdOrderByRequestTimeDesc(userId).orElse(null);
        return latestActivity;
    }

}
