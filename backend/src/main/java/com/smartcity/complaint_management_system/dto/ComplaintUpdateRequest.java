package com.smartcity.complaint_management_system.dto;

import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.enums.ComplaintType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintUpdateRequest {

    ComplaintStatus status;
    ComplaintType category;
    ComplaintPriority priority;
}
