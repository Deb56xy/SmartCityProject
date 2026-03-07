package com.smartcity.complaint_management_system.repository;

import com.smartcity.complaint_management_system.dto.OfficerPerformance;
import com.smartcity.complaint_management_system.enums.ComplaintPriority;
import com.smartcity.complaint_management_system.enums.ComplaintStatus;
import com.smartcity.complaint_management_system.model.Complaint;
import com.smartcity.complaint_management_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    List<Complaint> findAllByCitizenOrderByCreatedAtDesc(User user);
    Optional<Complaint> findByIdAndCitizenId(Long id, Long citizenId);

    @Query("""
        SELECT c FROM Complaint c
            WHERE (cast(:search AS text) IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%',cast(:search AS text),'%')))
            AND (:status IS NULL OR c.status = :status)
            AND (:priority IS NULL OR c.priority = :priority)
    """)
    Page<Complaint> searchComplaints(
            @Param("search") String search,
            @Param("status") ComplaintStatus status,
            @Param("priority") ComplaintPriority priority,
            Pageable pageable
    );

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("""
        SELECT d.name, COUNT(c) FROM Complaint c JOIN c.department d GROUP BY d.name
    """)
    List<Object[]> countByDepartment();

    @Query("""
        SELECT new com.smartcity.complaint_management_system.dto.OfficerPerformance(u.name,
        COUNT(s)) FROM Status s JOIN s.updatedBy u WHERE s.newStatus = 'RESOLVED' 
        GROUP BY u.name ORDER BY COUNT(s) DESC
    """)
    List<OfficerPerformance> topOfficers(Pageable pageable);
}
