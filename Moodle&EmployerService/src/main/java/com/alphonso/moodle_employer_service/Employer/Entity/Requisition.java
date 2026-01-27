package com.alphonso.moodle_employer_service.Employer.Entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requisition")
public class Requisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobRole;
    private Integer experienceRequired;
    
    @Column(name = "Employment_Type")
	@Enumerated(EnumType.STRING)
    private EmploymentType employmentType; 
    public enum EmploymentType {
    	@JsonProperty("FULL TIME")
    	 FULL_TIME,
    	 CONTRACT,
    	 INTERNSHIP
    	}
    
    private Location location;
    public enum Location {
    	@JsonProperty("NEW YORK")
    	 NEW_YORK,
    	 REMOTE,
    	 HYBRID
    	}
    
    private Integer openings;

    @Column(length = 3000)
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum Status {
        DRAFT, ACTIVE, PAUSED, CLOSED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @ManyToMany
    @JoinTable(
        name = "requisition_required_skill",
        joinColumns = @JoinColumn(name = "requisition_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<RequisitionSkill> requiredSkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "requisition_nice_skill",
        joinColumns = @JoinColumn(name = "requisition_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<RequisitionSkill> niceToHaveSkills = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}