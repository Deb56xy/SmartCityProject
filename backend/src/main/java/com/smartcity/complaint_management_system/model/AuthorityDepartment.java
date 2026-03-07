package com.smartcity.complaint_management_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "authority_department")
public class AuthorityDepartment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private Boolean active;
}

