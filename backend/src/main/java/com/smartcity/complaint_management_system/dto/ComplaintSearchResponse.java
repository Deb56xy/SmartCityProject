package com.smartcity.complaint_management_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComplaintSearchResponse {
    private Long id;
    private String title;
    private String category;
    private String ward;
    private String priority;
    private String status;
    private String assignedOfficer;
    private LocalDateTime createdDate;
    private LocalDateTime slaDeadline;
    private String createdBy;
    private String description;
    private String location;
    private LocalDateTime updatedDate;
}
