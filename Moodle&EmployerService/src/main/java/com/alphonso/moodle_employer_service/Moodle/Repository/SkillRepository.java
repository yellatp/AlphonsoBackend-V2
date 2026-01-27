package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillEntity;

@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {
    Optional<SkillEntity> findBySkillNameIgnoreCase(String skillName);
    @Query("SELECT s.skillName FROM SkillEntity s")
    List<String> findAllSkillNames();
}