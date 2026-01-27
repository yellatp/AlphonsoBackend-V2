package com.alphonso.profile_service.Entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Builder
public class ProfileDetails {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(unique = true)
    private String profileId;  
    
    private Long userId;

    private String firstName;
    
    private String lastName;

    @Email
    @NotBlank
    private String email;
    
    private String phoneNumber;

    @Column(unique = true)
    private String universityEmail;
    
    private boolean universityEmailVerified = false;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT; ///profileStatus
    
    public enum Status { DRAFT, SUBMITTED }

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt= LocalDateTime.now();
	}
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Enumerated(EnumType.STRING)
    private AssessmentStatus assessmentStatus = AssessmentStatus.MOODLE_PENDING;
    
    public enum AssessmentStatus { 
    	MOODLE_PENDING, 
        MOODLE_IN_PROGRESS,
        MOODLE_SYNC_SUCCESS,
        SUCCESS, //interviewer status default
        MOODLE_SYNC_FAILED, 
        QUIZ_PASSED,
        QUIZ_FAILED,
        WAITING_T1,
        COMPLETED_T1,
        REJECTED_T1,
        SHORT_LISTED}
    
    @Embedded
    private Address address;
    
    @Embedded
    private Portfolio portfolio;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApplicationQuestions applicationQuestion;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoluntaryDisclosure voluntaryDisclosure;
    
    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileSkills profileSkills;

}

