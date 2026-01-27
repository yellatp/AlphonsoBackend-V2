package com.alphonso.Interviewer_Service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback",
       indexes = {
         @Index(name = "idx_feedback_candidate", columnList = "candidate_profile_id"),
         @Index(name = "idx_feedback_interviewer", columnList = "interviewer_id"),
         @Index(name = "idx_feedback_interview", columnList = "interview_id")
       })
@Data
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_profile_id", nullable = false)
    private String candidateProfileId;

    @Column(name = "interviewer_id", nullable = false)
    private String interviewerId;

    @Column(name = "interview_id")
    private String interviewId;

    @Column(name = "analytical", nullable = false)
    private Integer analytical;   

    @Column(name = "technical", nullable = false)
    private Integer technical;       

    @Column(name = "design", nullable = false)
    private Integer design;            

    @Column(name = "execution", nullable = false)
    private Integer execution;    

    @Column(name = "communication", nullable = false)
    private Integer communication;   

    @Column(name = "collaboration", nullable = false)
    private Integer collaboration;   

    @Column(name = "adaptability", nullable = false)
    private Integer adaptability;  

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false)
    private Recommendation recommendation;
    
    public enum Recommendation {
        YES, NO
    }
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}