package com.alphonso.moodle_employer_service.Employer.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.CreateRequisitionRequest;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.ShortlistCandidateRequest;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.UpdateCandidateStageRequest;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateFullProfileViewResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateMatchResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.PipelineCandidateResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.RequisitionPipelineResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.RequisitionResponse;
import com.alphonso.moodle_employer_service.Employer.ServiceImp.EmployerPipelineService;
import com.alphonso.moodle_employer_service.Employer.ServiceImp.EmployerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerRequisitionController {

    private final EmployerService employerService;
    private final EmployerPipelineService pipelineService;

    @PostMapping("/requisition")
    public ResponseEntity<RequisitionResponse> create(
        @RequestBody CreateRequisitionRequest req, @RequestHeader("X-Email") String email) {

        return ResponseEntity.ok(employerService.createRequisition(email, req));
    }

    @GetMapping("/{requisitionId}/matches")
    public ResponseEntity<List<CandidateMatchResponse>> matchCandidates(
        @PathVariable Long requisitionId, @RequestHeader("X-Email") String email) {

        return ResponseEntity.ok(employerService.matchCandidates(requisitionId, email));
    }
    
    @GetMapping("/candidateReport/{profileId}")
	public ResponseEntity<CandidateFullProfileViewResponse> getUserReport(@PathVariable String profileId, @RequestHeader("X-Email") String email) {
    	CandidateFullProfileViewResponse dto = employerService.getCandidateFullProfileView(profileId);
		return ResponseEntity.ok(dto);
	}
    
    @PostMapping("/{requisitionId}/shortlist")
    public ResponseEntity<PipelineCandidateResponse> shortlistCandidate(
            @PathVariable Long requisitionId,
            @RequestHeader("X-Email") String employerEmail,
            @RequestBody ShortlistCandidateRequest req
    ) {
        return ResponseEntity.ok(pipelineService.shortlistCandidate(requisitionId, employerEmail, req));
    }

    @PutMapping("/{requisitionId}/pipeline")
    public ResponseEntity<PipelineCandidateResponse> updateCandidateStage(
            @PathVariable Long requisitionId,
            @RequestHeader("X-Email") String employerEmail,
            @RequestBody UpdateCandidateStageRequest req
    ) {
        return ResponseEntity.ok(pipelineService.updateCandidateStage(requisitionId, employerEmail, req));
    }

    @GetMapping("/{requisitionId}/pipeline")
    public ResponseEntity<RequisitionPipelineResponse> getPipeline(
            @PathVariable Long requisitionId,
            @RequestHeader("X-Email") String employerEmail
    ) {
        return ResponseEntity.ok(pipelineService.getRequisitionPipeline(requisitionId, employerEmail));
    }
}