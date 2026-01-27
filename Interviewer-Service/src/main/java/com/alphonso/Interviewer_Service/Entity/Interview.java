package com.alphonso.Interviewer_Service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "interview")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interview_id", nullable = false, unique = true)
    private String interviewId;

    @Column(name = "candidate_profile_id", nullable = false)
    private String candidateProfileId;

    @Column(name = "interviewer_id", nullable = false)
    private Long interviewerId;
    
    @Column(name = "candidate_email", nullable = false)
    private String candidateEmail;
    
    @Column(name = "interviewer_email", nullable = false)
    private String interviewerEmail;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "meet_url", length = 1000)
    private String meetUrl;

    @Column(name = "domain", length = 200)
    private String domain;
}