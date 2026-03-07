package com.smartcity.complaint_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {

    private String name;
    private String email;
    private String phone;
    private String location;
    private String district;
    private String state;
}
