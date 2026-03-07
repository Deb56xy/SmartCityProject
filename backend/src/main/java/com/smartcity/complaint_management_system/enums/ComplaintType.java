package com.smartcity.complaint_management_system.enums;

public enum ComplaintType {
    SERVICE (1),
    BILLING (2),
    TECHNICAL (3),
    BEHAVIOUR (4),
    OTHER (5);

    private final long departmentId;

    ComplaintType(long departmentId) {
        this.departmentId = departmentId;
    }

    public static long getDepartmentId(ComplaintType complaintType) {
        return complaintType.departmentId;
    }
}
