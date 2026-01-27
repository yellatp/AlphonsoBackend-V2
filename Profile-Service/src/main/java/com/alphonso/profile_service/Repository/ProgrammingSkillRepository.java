package com.alphonso.profile_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.ProgrammingSkill;
import java.util.Optional;

@Repository
public interface ProgrammingSkillRepository extends JpaRepository<ProgrammingSkill, Long> {

    Optional<ProgrammingSkill> findByProgramName(String programName);
}
