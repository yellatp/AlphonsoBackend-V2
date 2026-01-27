package com.alphonso.moodle_employer_service.Moodle.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentAttemptCategoryView;
import java.time.LocalDateTime;
import java.util.List;

public interface AssessmentAttemptCategoryViewRepository extends JpaRepository<AssessmentAttemptCategoryView, Long> {

    List<AssessmentAttemptCategoryView> findByMoodleUserId(Integer moodleUserId);

    List<AssessmentAttemptCategoryView> findByProfileId(String profileId);

    List<AssessmentAttemptCategoryView> findByAttemptId(Long attemptId);

    @Query(value = "SELECT DISTINCT v.profileId FROM AssessmentAttemptCategoryView v " +
            "WHERE (:since IS NULL OR v.attemptDate >= :since)")
    List<String> findDistinctProfileIdsSince(@Param("since") LocalDateTime since);
    
    
    List<AssessmentAttemptCategoryView> findByEmailIgnoreCase(String email);
    
    List<AssessmentAttemptCategoryView> findByProfileIdOrderByCategoryIdAsc(String profileId);

}
