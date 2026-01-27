package com.alphonso.Interviewer_Service.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.alphonso.Interviewer_Service.Entity.Interview;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    boolean existsByInterviewId(String interviewId);
    
    Optional<Interview> findByInterviewId(String interviewId);

    @Query("SELECT i FROM Interview i " +
           "WHERE i.interviewerEmail = :email " +
           "AND DATE(i.startTime) = CURRENT_DATE")
    List<Interview> findTodayInterviews(String email);

    @Query("SELECT i FROM Interview i " +
           "WHERE i.interviewerEmail = :email " +
           "AND i.startTime > CURRENT_TIMESTAMP")
    List<Interview> findUpcomingInterviews(String email);
    
    @Query("SELECT i FROM Interview i " +
           "WHERE i.candidateEmail = :email " +
           "AND DATE(i.startTime) = CURRENT_DATE")
    List<Interview> findTodayInterviewsByCandidateEmail(String email);
    
    @Query("SELECT i FROM Interview i " +
           "WHERE i.candidateEmail = :email " +
           "AND i.startTime > CURRENT_TIMESTAMP")
    List<Interview> findUpcomingInterviewsByCandidateEmail(String email);
}
