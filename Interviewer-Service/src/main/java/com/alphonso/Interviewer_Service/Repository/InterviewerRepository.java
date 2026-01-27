package com.alphonso.Interviewer_Service.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.alphonso.Interviewer_Service.Entity.InterviewerDetails;

public interface InterviewerRepository extends JpaRepository<InterviewerDetails, Long> {
    Optional<InterviewerDetails> findByEmail(String email);    
    @Query("select i from InterviewerDetails i where i.email is not null")
    List<InterviewerDetails> findAllForReminders();
}