package com.smartcity.complaint_management_system.model;

import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name ="status_logs")
@Getter
@Setter
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private ComplaintStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private ComplaintStatus newStatus;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    private User updatedBy;

    @ManyToOne
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority")
    private ComplaintPriority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority")
    private ComplaintPriority newPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "new_department",
            referencedColumnName = "id"
    )
    private Department newDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "old_department",
            referencedColumnName = "id"
    )
    private Department oldDepartment;
}
