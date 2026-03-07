package com.smartcity.complaint_management_system.repository;

import com.smartcity.complaint_management_system.model.AuthorityDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.smartcity.complaint_management_system.dto.AuthorityDto;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<AuthorityDepartment, Long> {

    @Query("""
        SELECT new com.smartcity.complaint_management_system.dto.AuthorityDto(u.id, u.name)
        FROM AuthorityDepartment ad JOIN ad.user u WHERE ad.department.id = :deptId
        AND ad.active = true AND LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    List<AuthorityDto> searchAuthorities(
        @Param("deptId") Long departmentId,
        @Param("q") String query);
}
