package com.smartcity.complaint_management_system.service;

import com.smartcity.complaint_management_system.dto.*;
import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.enums.ComplaintType;
import com.smartcity.complaint_management_system.mapper.ComplaintMapper;
import com.smartcity.complaint_management_system.model.*;
import com.smartcity.complaint_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.smartcity.complaint_management_system.mapper.ComplaintMapper.mapToActiveResponse;
import static com.smartcity.complaint_management_system.mapper.ComplaintMapper.mapToResolvedResponse;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private MlPredictionService mlPredictionService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Value("${MINIMUM_CONFIDENCE_THRESHOLD}")
    private double minConfidenceThreshold;

    @Value("${MAXIMUM_CONFIDENCE_THRESHOLD}")
    private double maxConfidenceThreshold;

    public Complaint create(String email, Complaint complaint) {

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));
        complaint.setCitizen(user);

        ComplaintType complaintType = this.predictComplaintType(complaint);
        Department department = departmentRepository
                .findById(ComplaintType.getDepartmentId(complaintType))
                .orElseThrow(() -> new RuntimeException("No department found"));
        complaint.setDepartment(department);
        this.setComplaintStatus(complaint);

        if(!complaint.getStatus().equals(ComplaintStatus.UNDER_REVIEW)) {
            this.setComplaintPriority(complaint);
        } else {
            complaint.setPriority(ComplaintPriority.LOW);
        }
        Complaint savedComplaint = complaintRepository.save(complaint);
        mailService.sendComplaintRegisteredMail(user.getName(),
                savedComplaint.getId(), savedComplaint.getStatus().name(), user.getEmail());
        mailService.sendComplaintRegisteredAuthorityMail(savedComplaint,
                department.getEmail(), user.getName());
        return savedComplaint;
    }

    public Map<String, List<ComplaintResponse>> getAllComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));

        List<Complaint> complaintList = complaintRepository.findAllByCitizenOrderByCreatedAtDesc(user);
        List<Status> statusList = statusRepository.findAllByComplaintIn(complaintList);

        List<ComplaintResponse> ongoing = new ArrayList<>();
        List<ComplaintResponse> resolved = new ArrayList<>();

        for (Complaint c : complaintList) {
            Status status = statusList.stream().filter(status1 -> status1.getComplaint().equals(c))
                    .findFirst().orElse(null);
            if (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.REJECTED) {
                resolved.add(mapToResolvedResponse(c, status));
            } else {
                ongoing.add(mapToActiveResponse(c, status));
            }
        }

        return Map.of(
                "ongoingComplaints", ongoing,
                "resolvedComplaints", resolved
        );
    }

    @Transactional
    public void deleteComplaint(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));
        Complaint complaint = complaintRepository.findByIdAndCitizenId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Complaint is not assigned to the user"));

        statusRepository.deleteByComplaint(complaint);
        complaintRepository.delete(complaint);
    }

    public void reopenComplaint(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));
        Complaint complaint = complaintRepository.findByIdAndCitizenId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Complaint is not assigned to the user"));

        ComplaintType complaintType = this.predictComplaintType(complaint);
        Department department = departmentRepository
                .findById(ComplaintType.getDepartmentId(complaintType))
                .orElseThrow(() -> new RuntimeException("No department found"));
        complaint.setDepartment(department);
        this.setComplaintStatus(complaint);

        if(!complaint.getStatus().equals(ComplaintStatus.UNDER_REVIEW)) {
            this.setComplaintPriority(complaint);
        } else {
            complaint.setPriority(ComplaintPriority.LOW);
        }

        complaint.setCreatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        Status status = statusRepository.findAllByComplaintIn(List.of(complaint)).getFirst();
        statusRepository.delete(status);
    }

    private void setComplaintStatus(Complaint complaint) {
        MlPredictionResponse predictionResponse = mlPredictionService
                .predictComplaintValidity(complaint.getDescription());
        String prediction = predictionResponse.getPrediction();
        double confidence = predictionResponse.getConfidence();

        if(Objects.isNull(prediction) || ("FAKE".equalsIgnoreCase(prediction) && confidence >= minConfidenceThreshold)) {
            complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
        } else {
            complaint.setStatus(ComplaintStatus.NEW);
        }
    }

    private void setComplaintPriority(Complaint complaint) {
        MlPredictionResponse predictionResponse = mlPredictionService
                .predictComplaintPriority(complaint.getDescription());
        String prediction = predictionResponse.getPrediction();
        double confidence = predictionResponse.getConfidence();

        if(Objects.isNull(prediction)) {
            complaint.setPriority(ComplaintPriority.LOW);
        } else if(confidence <= maxConfidenceThreshold && confidence >= minConfidenceThreshold) {
            ComplaintPriority suggestedPriority = ComplaintPriority.getByName(prediction);
            complaint.setPriority(ComplaintPriority.getByLevel(suggestedPriority.level - 1));
        } else if(confidence < minConfidenceThreshold) {
            ComplaintPriority suggestedPriority = ComplaintPriority.getByName(prediction);
            complaint.setPriority(ComplaintPriority.getByLevel(suggestedPriority.level - 2));
        } else {
            complaint.setPriority(ComplaintPriority.getByName(prediction));
        }
    }

    private ComplaintType predictComplaintType(Complaint complaint) {
        MlPredictionResponse predictionResponse = mlPredictionService
                .predictComplaintCategory(complaint.getDescription());
        String prediction = predictionResponse.getPrediction();
        double confidence = predictionResponse.getConfidence();
        String userComplaintType = complaint.getComplaintType().name();

        if(Objects.isNull(prediction)) {
            return ComplaintType.OTHER;
        } else if(userComplaintType.equalsIgnoreCase(prediction)) {
            return complaint.getComplaintType();
        } else if(confidence <= minConfidenceThreshold) {
            return complaint.getComplaintType();
        } else {
            return ComplaintType.valueOf(prediction);
        }
    }

    public Page<ComplaintSearchResponse> searchComplaints(String search, String status, String priority, Pageable pageable) {

        ComplaintStatus complaintStatus = empty(status) ? null : ComplaintStatus.valueOf(status);
        ComplaintPriority complaintPriority = empty(priority) ? null : ComplaintPriority.valueOf(priority);
        Page<Complaint> complaintList =  complaintRepository.searchComplaints(empty(search) ? null : search,
                complaintStatus, complaintPriority, pageable);
        List<Status> statusList = statusRepository.findAllByComplaintIn(complaintList.getContent());
        Map<Long, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(s -> s.getComplaint().getId(), Function.identity()));
        List<ComplaintSearchResponse> responses = complaintList.getContent().stream().map(c ->
                ComplaintMapper.mapToSearchResponse(c, statusMap.get(c.getId())))
                .sorted(Comparator.comparing(ComplaintSearchResponse::getAssignedOfficer,
                        Comparator.nullsFirst(Comparator.naturalOrder()))).toList();

        return new PageImpl<>(responses, complaintList.getPageable(), complaintList.getTotalElements());
    }

    private boolean empty(String str) {
        return (Objects.isNull(str) || str.isBlank());
    }

    public Map<String, Integer> stats() {
        List<Complaint> complaints = complaintRepository.findAll();
        List<Complaint> newComplaints = complaints.stream()
                .filter(c -> c.getStatus().equals(ComplaintStatus.NEW)).toList();
        List<Complaint> inProgressComplaints = complaints.stream()
                .filter(c -> c.getStatus().equals(ComplaintStatus.IN_PROGRESS)).toList();
        List<Complaint> resolvedComplaintList = complaints.stream()
                .filter(c -> c.getStatus().equals(ComplaintStatus.RESOLVED)).toList();
        List<Complaint> underReviewComplaints = complaints.stream()
                .filter(c -> c.getStatus().equals(ComplaintStatus.UNDER_REVIEW)).toList();

        List<Complaint> overDueComplaints = new ArrayList<>();
        for(Complaint c : complaints) {
            if(!c.getStatus().equals(ComplaintStatus.RESOLVED) && !c.getStatus().equals(ComplaintStatus.REJECTED)) {
                LocalDateTime createdAt = c.getCreatedAt();
                LocalDateTime now = LocalDateTime.now();
                if(c.getPriority().equals(ComplaintPriority.LOW)) {
                    if(createdAt.isBefore(now.minusDays(7)))
                        overDueComplaints.add(c);
                }
                if(c.getPriority().equals(ComplaintPriority.MEDIUM)) {
                    if(createdAt.isBefore(now.minusDays(5)))
                        overDueComplaints.add(c);
                }
                if(c.getPriority().equals(ComplaintPriority.HIGH)) {
                    if(createdAt.isBefore(now.minusDays(7)))
                        overDueComplaints.add(c);
                }
            }
        }

        return Map.of(
                "total", complaints.size(),
                "new", newComplaints.size(),
                "underReview", underReviewComplaints.size(),
                "inProgress", inProgressComplaints.size(),
                "resolved", resolvedComplaintList.size(),
                "overdue", overDueComplaints.size()
        );
    }

    public AnalyticsResponse getAnalytics() {
        AnalyticsResponse response = new AnalyticsResponse();
        response.byStatus = ComplaintMapper.mapToAnalytics(complaintRepository.countByStatus());
        response.byType = ComplaintMapper.mapToAnalytics(complaintRepository.countByDepartment());
        response.topOfficers = complaintRepository.topOfficers(PageRequest.of(0, 5));
        return response;
    }

    public void assignComplaint(Long complaintId, Long authorityId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("No complaint found with this ID"));

        User authority = userRepository.findById(authorityId)
                .orElseThrow(() -> new RuntimeException("No user found"));

        List<Status> statusList = statusRepository.findAllByComplaintIn(List.of(complaint));
        Status status;
        if(statusList.isEmpty()) {
            status = new Status();
        } else {
            status = statusList.getFirst();
        }
        status.setComplaint(complaint);
        status.setUpdatedBy(authority);
        status.setUpdatedAt(LocalDateTime.now());
        statusRepository.save(status);
    }

    public List<AuthorityDto> searchAuthority(Long complaintId, String query) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("No complaint found with this ID"));

        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return authorityRepository.searchAuthorities(complaint.getDepartment().getId(), query.trim());
    }

    public Page<ComplaintSearchResponse> getOngoingComplaintsByAuthority(String email, Pageable pageable) {

        User authority = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));
        Page<Status> statusList = statusRepository
                .findByUpdatedByAndComplaint_StatusNotIn(authority,
                        List.of(ComplaintStatus.RESOLVED, ComplaintStatus.REJECTED), pageable);
        return statusList
                .map(status -> ComplaintMapper.mapToSearchResponse(status.getComplaint(), status));
    }

    public Page<ComplaintSearchResponse> getCompletedComplaintsByAuthority(String email, Pageable pageable) {

        User authority = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));
        Page<Status> statusList = statusRepository
                .findByUpdatedByAndComplaint_StatusIn(authority,
                        List.of(ComplaintStatus.RESOLVED, ComplaintStatus.REJECTED), pageable);
        return statusList
                .map(status -> ComplaintMapper.mapToSearchResponse(status.getComplaint(), status));
    }

    public void updateComplaint(Long complaintId, ComplaintUpdateRequest request, String email) {

        User authority = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("No complaint found with the given ID"));

        Department department = departmentRepository.findByName(request.getCategory().name())
                .orElseThrow(() -> new RuntimeException("No Department found"));

        Status complaintStatus = statusRepository.findByComplaint(complaint)
                        .orElse(new Status());

        complaintStatus.setOldStatus(complaint.getStatus());
        complaintStatus.setOldDepartment(complaint.getDepartment());
        complaintStatus.setOldPriority(complaint.getPriority());
        complaintStatus.setUpdatedAt(LocalDateTime.now());
        complaintStatus.setUpdatedBy(authority);
        complaintStatus.setNewStatus(request.getStatus());
        complaintStatus.setNewPriority(request.getPriority());
        complaintStatus.setNewDepartment(department);

        complaint.setPriority(request.getPriority());
        complaint.setDepartment(department);
        complaint.setStatus(request.getStatus());

        if(!request.getStatus().equals(ComplaintStatus.UNDER_REVIEW)) {
            complaint.setFake(Boolean.FALSE);
        }

        complaintRepository.save(complaint);
        statusRepository.save(complaintStatus);
    }
}
