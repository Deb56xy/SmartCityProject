package com.smartcity.complaint_management_system.mapper;

import com.smartcity.complaint_management_system.dto.ComplaintResponse;
import com.smartcity.complaint_management_system.dto.ComplaintSearchResponse;
import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.model.Complaint;
import com.smartcity.complaint_management_system.model.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComplaintMapper {

    public static ComplaintResponse mapToActiveResponse(Complaint c, Status status) {

        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setType(c.getDepartment().getName());
        dto.setStatus(c.getStatus().name());

        dto.setPendingWith(
                Objects.nonNull(status) && Objects.nonNull(status.getUpdatedBy()) ? status.getUpdatedBy().getName() : "-"
        );
        return dto;
    }

    public static ComplaintResponse mapToResolvedResponse(Complaint c, Status status) {

        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setType(c.getDepartment().getName());
        dto.setStatus(c.getStatus().name());

        if(Objects.nonNull(status)) {
            long days = Duration.between(c.getCreatedAt(), status.getUpdatedAt()).toDays();
            dto.setResolvedInDays(days);
        }
        dto.setResolvedBy(status.getUpdatedBy().getName());
        return dto;
    }

    public static ComplaintSearchResponse mapToSearchResponse(Complaint c, Status status) {

        ComplaintSearchResponse response = new ComplaintSearchResponse();
        response.setId(c.getId());
        response.setPriority(c.getPriority().name());
        response.setStatus(c.getStatus().name());
        response.setCategory(c.getDepartment().getName());
        response.setWard(c.getWard());
        response.setAssignedOfficer(Objects.nonNull(status)
                && Objects.nonNull(status.getUpdatedBy()) ? status.getUpdatedBy().getName() : "-");
        response.setCreatedDate(c.getCreatedAt());
        response.setSlaDeadline(getDeadLine(c.getPriority().name(), c.getCreatedAt()));
        response.setTitle(c.getTitle());
        response.setDescription(c.getDescription());
        response.setLocation(c.getLocality());
        response.setCreatedBy(c.getCitizen().getName());
        response.setUpdatedDate(Objects.nonNull(status)
                && Objects.nonNull(status.getUpdatedAt()) ? status.getUpdatedAt() : null);
        return response;
    }

    private static LocalDateTime getDeadLine(String priority, LocalDateTime createdDate) {
        if(priority.equalsIgnoreCase(ComplaintPriority.LOW.name()))
            return createdDate.plusDays(7);

        if(priority.equalsIgnoreCase(ComplaintPriority.MEDIUM.name()))
            return createdDate.plusDays(5);

        if(priority.equalsIgnoreCase(ComplaintPriority.HIGH.name()))
            return createdDate.plusDays(3);

        return createdDate;
    }

    public static Map<String, Long> mapToAnalytics(List<Object[]> list) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] o : list) {
            map.put(o[0].toString(), (Long) o[1]);
        }
        return map;
    }
}
