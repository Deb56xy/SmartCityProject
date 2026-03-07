package com.smartcity.complaint_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String status;

    private String pendingWith;

    private Long resolvedInDays;
    private String resolvedBy;
}

