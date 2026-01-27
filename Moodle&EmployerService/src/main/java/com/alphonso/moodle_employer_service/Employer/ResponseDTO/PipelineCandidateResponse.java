package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PipelineCandidateResponse {

    private String profileId;
    private String stage;

    private String assessmentLink;
    private String interviewLink;
    private String offerLetterLink;
    private String technicalInterviewerEmail;
}
