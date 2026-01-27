package com.alphonso.Interviewer_Service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.Interviewer_Service.Entity.Feedback;
import java.util.Optional;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByCandidateProfileIdOrderByCreatedAtDesc(String candidateProfileId);

    Optional<Feedback> findByInterviewId(String interviewId);

    Optional<Feedback> findFirstByCandidateProfileIdAndInterviewerIdOrderByCreatedAtDesc(String candidateProfileId, String interviewerId);
    
    Optional<Feedback> findTopByCandidateProfileIdOrderByCreatedAtDesc(String candidateProfileId);
}