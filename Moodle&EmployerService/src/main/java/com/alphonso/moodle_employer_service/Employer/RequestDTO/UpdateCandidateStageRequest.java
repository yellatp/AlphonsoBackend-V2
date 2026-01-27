package com.alphonso.moodle_employer_service.Employer.RequestDTO;

import lombok.Data;

@Data
public class UpdateCandidateStageRequest {

    private String profileId;
    private String stage; 

    private String assessmentLink;
    private String interviewLink;
    private String offerLetterLink;

    private String technicalInterviewerEmail;
}
