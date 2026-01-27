package com.alphonso.moodle_employer_service.Employer.Repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.moodle_employer_service.Employer.Entity.RequisitionSkill;

public interface RequisitionSkillRepository extends JpaRepository<RequisitionSkill, Long> {

    List<RequisitionSkill> findByIdIn(Set<Long> ids);
}