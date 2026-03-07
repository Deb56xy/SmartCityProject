package com.smartcity.complaint_management_system.controller;

import com.smartcity.complaint_management_system.dto.*;
import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.model.Complaint;
import com.smartcity.complaint_management_system.enums.ComplaintType;
import com.smartcity.complaint_management_system.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/types")
    public ComplaintType[] getComplaintTypes() {
        return ComplaintType.values();
    }

    @GetMapping("/status")
    public ComplaintStatus[] getComplaintStatus() {
        return ComplaintStatus.values();
    }

    @GetMapping("/priorities")
    public ComplaintPriority[] getComplaintPriorities() {
        return ComplaintPriority.values();
    }

    @PostMapping
    public Complaint create(@Valid @RequestBody Complaint complaint,
                            Authentication authentication) {
        String email = authentication.getName();
        return complaintService.create(email, complaint);
    }

    @GetMapping
    public Map<String, List<ComplaintResponse>> getAllComplaints(Authentication authentication) {
        String email = authentication.getName();
        return complaintService.getAllComplaints(email);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable Long id,
                                Authentication authentication) {
        String email = authentication.getName();
        complaintService.deleteComplaint(id, email);
    }

    @PostMapping("/{id}/reopen")
    public void reopenComplaints(@PathVariable Long id,
                                 Authentication authentication) {
        String email = authentication.getName();
        complaintService.reopenComplaint(id, email);
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return complaintService.stats();
    }

    @GetMapping("/search")
    public Page<ComplaintSearchResponse> searchComplaints(
                                        @RequestParam(defaultValue="") String search,
                                        @RequestParam(defaultValue="") String status,
                                        @RequestParam(defaultValue="") String priority,
                                        @RequestParam int page,
                                        @RequestParam int size) {
        return complaintService.searchComplaints(
                search, status, priority, PageRequest.of(page, size)
        );
    }

    @GetMapping("/analytics")
    public AnalyticsResponse analytics() {
        return complaintService.getAnalytics();
    }

    @GetMapping("/search/authority")
    public List<AuthorityDto> searchAuthority(@RequestParam Long complaintId, @RequestParam String q) {
        return complaintService.searchAuthority(complaintId, q);
    }

    @PostMapping("/{complaintId}/assign")
    public void assignComplaint(@PathVariable Long complaintId,
                                @RequestParam Long authorityId) {
        complaintService.assignComplaint(complaintId, authorityId);
    }

    @GetMapping("/authority/ongoing")
    public Page<ComplaintSearchResponse> getOngoingComplaintsByAuthority(@RequestParam int page,
                                                                  @RequestParam int size,
                                                                  Authentication authentication) {
        String email = authentication.getName();
        return complaintService.getOngoingComplaintsByAuthority(email, PageRequest.of(page, size));
    }

    @GetMapping("/authority/completed")
    public Page<ComplaintSearchResponse> getCompletedComplaintsByAuthority(@RequestParam int page,
                                                                  @RequestParam int size,
                                                                  Authentication authentication) {
        String email = authentication.getName();
        return complaintService.getCompletedComplaintsByAuthority(email, PageRequest.of(page, size));
    }

    @PostMapping("/update/{complaintId}")
    public String updateComplaint(@PathVariable Long complaintId,
                                  @RequestBody ComplaintUpdateRequest request,
                                  Authentication authentication) {
        String email = authentication.getName();
        complaintService.updateComplaint(complaintId, request, email);
        return "Status updated successfully";
    }
}
