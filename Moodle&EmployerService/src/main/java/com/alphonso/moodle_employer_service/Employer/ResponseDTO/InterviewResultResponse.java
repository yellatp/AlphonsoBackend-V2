package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewResultResponse {

    private String candidateProfileId;

    private Double interviewPercentage;

    private Double adaptabilityPercentage;
    private Double analyticalPercentage;
    private Double collaborationPercentage;
    private Double communicationPercentage;
    private Double designPercentage;
    private Double executionPercentage;
    private Double technicalPercentage;
}
