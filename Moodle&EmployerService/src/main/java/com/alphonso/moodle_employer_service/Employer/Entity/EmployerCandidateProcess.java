package com.alphonso.moodle_employer_service.Employer.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "employer_candidate_process",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employer_id", "requisition_id", "profile_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerCandidateProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private Requisition requisition;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    private String assessmentLink;
    private String interviewLink;
    private String offerLetterLink;

    private String technicalInterviewerEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Stage {
        SHORTLISTED,
        ASSESSMENT,
        INTERVIEW,
        OFFERED,
        OFFER_ACCEPTED,
        OFFER_REJECTED,
        REJECTED,
        DROPPED
    }
}
