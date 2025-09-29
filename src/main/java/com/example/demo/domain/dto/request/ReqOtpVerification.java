package com.example.demo.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqOtpVerification {

    private String email;
    private String type; // Registration or Reset Password
    private String otp;

}
