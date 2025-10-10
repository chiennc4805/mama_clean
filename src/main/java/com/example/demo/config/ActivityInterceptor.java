package com.example.demo.config;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.domain.User;
import com.example.demo.domain.UserActivity;
import com.example.demo.service.UserActivityService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private final UserActivityService userActivityService;
    private final UserService userService;

    public ActivityInterceptor(UserActivityService userActivityService, UserService userService) {
        this.userActivityService = userActivityService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Lấy JWT từ header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Lấy username từ JWT
            String username = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                    : null;
            if (username != null) {
                User user = this.userService.fetchUserByEmail(username);

                UserActivity userActivity = new UserActivity();
                userActivity.setUser(user);
                userActivity.setAction(request.getRequestURI());
                userActivity.setRequestTime(LocalDateTime.now());
                userActivityService.create(userActivity);
            }
        }

        return true; // tiếp tục request
    }
}
