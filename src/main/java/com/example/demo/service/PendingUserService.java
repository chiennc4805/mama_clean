package com.example.demo.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.domain.PendingUser;
import com.example.demo.domain.Role;
import com.example.demo.repository.PendingUserRepository;

@Service
public class PendingUserService {

    private final PendingUserRepository pendingUserRepository;
    // private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RoleService roleService;

    public PendingUserService(PendingUserRepository pendingUserRepository, PasswordEncoder passwordEncoder,
            JavaMailSender mailSender, RoleService roleService) {
        this.pendingUserRepository = pendingUserRepository;
        // this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.roleService = roleService;
    }

    public PendingUser create(PendingUser pendingUser) {
        if (pendingUser.getRole() != null) {
            Role role = this.roleService.handleFetchRoleByName(pendingUser.getRole().getName());
            pendingUser.setRole(role);
        }
        return this.pendingUserRepository.save(pendingUser);
    }

    public PendingUser fetchByEmail(String email) {
        Optional<PendingUser> optionalPendingUser = this.pendingUserRepository.findByEmail(email);
        return optionalPendingUser.isPresent() ? optionalPendingUser.get() : null;
    }

    public void delete(String id) {
        this.pendingUserRepository.deleteById(id);
    }

    public PendingUser generateAndSendOtp(PendingUser pendingUser, String type) {
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000); // 6-digit OTP
        String otp = String.valueOf(otpValue);

        pendingUser.setOtpCode(otp);
        pendingUser.setOtpRequestedTime(LocalDateTime.now());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mamascleann@gmail.com");
        message.setTo(pendingUser.getEmail());
        message.setSubject("Your OTP for " + type);
        message.setText("Your One-Time Password (OTP) is: " + otp + ". It is valid for 5 minutes.");
        mailSender.send(message);

        return pendingUser;
    }

    public String generateAndSendNewPassword(String email) {
        // generate random password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        String newPassword = sb.toString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mamascleann@gmail.com");
        message.setTo(email);
        message.setSubject("Your New Password for Login");
        message.setText("Your New Password is: " + newPassword);
        mailSender.send(message);

        return newPassword;
    }

    public boolean validateOtp(PendingUser pendingUser, String reqOtp) {

        String storedOtp = pendingUser.getOtpCode();
        LocalDateTime expiryTime = pendingUser.getOtpRequestedTime().plusMinutes(5);

        if (storedOtp != null && expiryTime != null && LocalDateTime.now().isBefore(expiryTime)) {
            return storedOtp.equals(reqOtp);
        }
        return false;
    }

}
