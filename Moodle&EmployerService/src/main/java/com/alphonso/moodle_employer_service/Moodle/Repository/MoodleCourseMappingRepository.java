package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleCourseMappingEntity;

@Repository
public interface MoodleCourseMappingRepository extends JpaRepository<MoodleCourseMappingEntity, Long> {
    Optional<MoodleCourseMappingEntity> findByMoodleCourseId(Long moodleCourseId);
    boolean existsByMoodleCourseId(Long moodleCourseId);
    boolean existsByMoodleCourseIdAndCohortId(Long moodleCourseId, Long cohortId);
    Optional<MoodleCourseMappingEntity> findBySkillName(String skillName);
}