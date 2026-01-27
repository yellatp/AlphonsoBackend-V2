package com.alphonso.profile_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.CoreSkills;
import java.util.Optional;

@Repository
public interface SkillMasterRepository extends JpaRepository<CoreSkills, Long> {
    Optional<CoreSkills> findBySkillNameIgnoreCase(String skillName);
}
