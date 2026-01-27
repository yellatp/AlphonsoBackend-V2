package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.AssessmentResultResponse;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CandidateFullProfileViewResponse {

    private CandidateProfileLeftViewResponse profile;

    private Double overAllPercentage;
    
    private AssessmentResultResponse assessment;

    
    private InterviewResultResponse interview;
}
