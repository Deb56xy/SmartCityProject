package com.smartcity.complaint_management_system.model;

import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.enums.ComplaintType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name ="complaints")
@Getter
@Setter
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    @Column(length = 1000)
    private String description;

    private String locality;

    private String ward;

    private boolean fake;

    @Enumerated(EnumType.STRING)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @ManyToOne
    private User citizen;

    @ManyToOne
    private Department department;

    private LocalDateTime createdAt=LocalDateTime.now();

    @Transient
    private ComplaintType complaintType;
}
