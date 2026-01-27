package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentAttempt;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    Optional<AssessmentAttempt> findByMoodleAttemptId(Long moodleAttemptId);
    
    @Query("SELECT a.score FROM AssessmentAttempt a WHERE a.moodleUserId = :moodleUserId ORDER BY a.attemptDate DESC")
    Double findScoresByMoodleUserId(@Param("moodleUserId") Integer moodleUserId);

    
    AssessmentAttempt findTopByMoodleUserIdOrderByAttemptDateDesc(Integer moodleUserId);
    boolean existsByMoodleAttemptId(Long attemptId);
    
    boolean existsByMoodleUserIdAndMoodleQuizIdAndAttemptDate(Integer moodleUserId,
                                                              Integer moodleQuizId,
                                                              LocalDateTime attemptDate);

    boolean existsByMoodleUserIdAndMoodleQuizIdAndAttemptDateBetween(Integer moodleUserId,
                                                                     Integer moodleQuizId,
                                                                     LocalDateTime start,
                                                                     LocalDateTime end);
}

