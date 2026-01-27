package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequisitionPipelineResponse {

    private Long requisitionId;
    private String jobRole;
    private Integer openings;
    private String location;
    private String employmentType;
    private String status;

    private List<PipelineCandidateResponse> shortlisted;
    private List<PipelineCandidateResponse> assessment;
    private List<PipelineCandidateResponse> interview;
    private List<PipelineCandidateResponse> offered;
    private List<PipelineCandidateResponse> offerAccepted;
    private List<PipelineCandidateResponse> offerRejected;
    private List<PipelineCandidateResponse> rejected;
}
