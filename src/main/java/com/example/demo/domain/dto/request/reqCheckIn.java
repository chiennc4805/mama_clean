package com.example.demo.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class reqCheckIn {

    private double customerLat;
    private double customerLon;
    private double cleanerLat;
    private double cleanerLon;

}
