package com.oauth2.User.Hospital.dto;

import lombok.Data;

@Data
public class HospitalRegistrationResponse {
    private Long id;
    private String hospitalCode;
    private String hospitalName;
    private String hospitalAddress;
}
