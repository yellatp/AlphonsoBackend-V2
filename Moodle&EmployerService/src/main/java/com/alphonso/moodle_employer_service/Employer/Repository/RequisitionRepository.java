package com.alphonso.moodle_employer_service.Employer.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.moodle_employer_service.Employer.Entity.Requisition;

public interface RequisitionRepository extends JpaRepository<Requisition, Long> {

    List<Requisition> findByEmployerId(Long employerId);
}