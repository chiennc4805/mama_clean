package com.example.demo.domain.dto.request;

import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCleanerCreation {

    private User userProfile;
    private CleanerProfile cleanerProfile;

}
