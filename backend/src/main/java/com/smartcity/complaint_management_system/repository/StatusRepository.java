package com.smartcity.complaint_management_system.repository;

import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.model.Complaint;
import com.smartcity.complaint_management_system.model.Status;
import com.smartcity.complaint_management_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findAllByComplaintIn(List<Complaint> complaintList);
    void deleteByComplaint(Complaint complaint);
    Page<Status> findByUpdatedByAndComplaint_StatusNotIn(User authority, List<ComplaintStatus> statuses, Pageable pageable);
    Page<Status> findByUpdatedByAndComplaint_StatusIn(User authority, List<ComplaintStatus> statuses, Pageable pageable);
    Optional<Status> findByComplaint(Complaint complaint);
}
