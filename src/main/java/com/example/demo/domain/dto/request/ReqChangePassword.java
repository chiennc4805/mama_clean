package com.example.demo.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqChangePassword {

    private String userId;
    private String currentPassword;
    private String newPassword;

}
