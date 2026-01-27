package com.alphonso.Interviewer_Service.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
