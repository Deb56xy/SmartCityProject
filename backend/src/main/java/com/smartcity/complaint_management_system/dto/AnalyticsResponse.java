package com.smartcity.complaint_management_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AnalyticsResponse {

    public Map<String, Long> byStatus;
    public Map<String, Long> byType;
    public List<OfficerPerformance> topOfficers;
}
