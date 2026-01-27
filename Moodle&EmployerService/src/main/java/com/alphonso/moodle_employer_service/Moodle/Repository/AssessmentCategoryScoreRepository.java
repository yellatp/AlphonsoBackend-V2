package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentCategoryScore;

@Repository
public interface AssessmentCategoryScoreRepository extends JpaRepository<AssessmentCategoryScore, Long> {
    List<AssessmentCategoryScore> findByAttemptRefId(Long attemptRefId);
}