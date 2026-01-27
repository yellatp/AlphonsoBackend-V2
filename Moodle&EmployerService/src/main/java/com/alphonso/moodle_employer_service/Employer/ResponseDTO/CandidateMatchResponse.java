package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateMatchResponse {

    private String candidateProfileId;
    private String candidateName;
    private String candidateRole;
    private Integer totalExperience;        
    private Double skillMatchPercentage;   
    private String email;
}
