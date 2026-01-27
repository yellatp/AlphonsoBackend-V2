package com.alphonso.profile_service.Entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "application_questions")
public class ApplicationQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Legal_Age_To_Work")
    private Boolean legalAgeToWork;                
    
    @Column(name = "Job_Preference")
	@Enumerated(EnumType.STRING)
    private JobPreference jobPreference; 
    
    public enum JobPreference {
    	@JsonProperty("FULL TIME")
    	 FULL_TIME,
    	 CONTRACT,
    	 INTERNSHIP
    	}
    
    @Column(name = "willing_To_Relocate")
    private Boolean willingToRelocate;
    
    @Column(name = "requires_Sponsorship")
    private Boolean requiresSponsorship;    
    
    
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ProfileDetails profile;
}