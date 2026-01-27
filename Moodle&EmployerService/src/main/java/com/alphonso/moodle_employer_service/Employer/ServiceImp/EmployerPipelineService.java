package com.alphonso.moodle_employer_service.Employer.ServiceImp;

import com.alphonso.moodle_employer_service.Employer.Entity.Employer;
import com.alphonso.moodle_employer_service.Employer.Entity.EmployerCandidateProcess;
import com.alphonso.moodle_employer_service.Employer.Entity.Requisition;
import com.alphonso.moodle_employer_service.Employer.Entity.EmployerCandidateProcess.Stage;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.PipelineException;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.UnauthorizedEmployerException;
import com.alphonso.moodle_employer_service.Employer.Repository.EmployerCandidateProcessRepository;
import com.alphonso.moodle_employer_service.Employer.Repository.EmployerRepository;
import com.alphonso.moodle_employer_service.Employer.Repository.RequisitionRepository;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.ShortlistCandidateRequest;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.UpdateCandidateStageRequest;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.PipelineCandidateResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.RequisitionPipelineResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerPipelineService {

    private final EmployerRepository employerRepo;
    private final RequisitionRepository requisitionRepo;
    private final EmployerCandidateProcessRepository processRepo;
    //private final EmployerEmailService emailService;

    @Transactional
    public PipelineCandidateResponse shortlistCandidate(Long requisitionId, String employerEmail, ShortlistCandidateRequest req) {

        Employer employer = employerRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new PipelineException("Employer not found"));

        Requisition requisition = requisitionRepo.findById(requisitionId)
                .orElseThrow(() -> new PipelineException("Requisition not found"));

        if (!Objects.equals(requisition.getEmployer().getId(), employer.getId())) {
            throw new UnauthorizedEmployerException("Unauthorized requisition access");
        }

        if (req.getProfileId() == null || req.getProfileId().isBlank()) {
            throw new PipelineException("profileId is required");
        }

        EmployerCandidateProcess process = processRepo
                .findByEmployer_IdAndRequisition_IdAndProfileId(employer.getId(), requisitionId, req.getProfileId())
                .orElseGet(() -> EmployerCandidateProcess.builder()
                        .employer(employer)
                        .requisition(requisition)
                        .profileId(req.getProfileId())
                        .build());

        process.setStage(Stage.SHORTLISTED);
        EmployerCandidateProcess saved = processRepo.save(process);

        return mapToResponse(saved);
    }
    
    @Transactional
    public PipelineCandidateResponse updateCandidateStage(Long requisitionId, String employerEmail,
                                                         UpdateCandidateStageRequest req) {

        Employer employer = employerRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new PipelineException("Employer not found"));

        Requisition requisition = requisitionRepo.findById(requisitionId)
                .orElseThrow(() -> new PipelineException("Requisition not found"));

        if (!Objects.equals(requisition.getEmployer().getId(), employer.getId())) {
            throw new UnauthorizedEmployerException("Unauthorized requisition access");
        }

        if (req.getProfileId() == null || req.getProfileId().isBlank()) {
            throw new PipelineException("profileId is required");
        }

        Stage newStage;
        try {
            newStage = Stage.valueOf(req.getStage().trim().toUpperCase());
        } catch (Exception e) {
            throw new PipelineException("Invalid stage: " + req.getStage());
        }

        EmployerCandidateProcess process = processRepo
                .findByEmployer_IdAndRequisition_IdAndProfileId(employer.getId(), requisitionId, req.getProfileId())
                .orElseThrow(() -> new PipelineException("Candidate not found in requisition pipeline"));

        process.setStage(newStage);

        if (req.getAssessmentLink() != null) process.setAssessmentLink(req.getAssessmentLink());
        if (req.getInterviewLink() != null) process.setInterviewLink(req.getInterviewLink());
        if (req.getOfferLetterLink() != null) process.setOfferLetterLink(req.getOfferLetterLink());
        if (req.getTechnicalInterviewerEmail() != null) process.setTechnicalInterviewerEmail(req.getTechnicalInterviewerEmail());

        EmployerCandidateProcess saved = processRepo.save(process);

        // ✅ if offer accepted: drop candidate from other employer cycles + mail
        if (newStage == Stage.OFFER_ACCEPTED) {
            dropCandidateFromOtherEmployers(req.getProfileId(), employer.getId());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public RequisitionPipelineResponse getRequisitionPipeline(Long requisitionId, String employerEmail) {

        Employer employer = employerRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new PipelineException("Employer not found"));

        Requisition requisition = requisitionRepo.findById(requisitionId)
                .orElseThrow(() -> new PipelineException("Requisition not found"));

        if (!Objects.equals(requisition.getEmployer().getId(), employer.getId())) {
            throw new UnauthorizedEmployerException("Unauthorized requisition access");
        }

        List<EmployerCandidateProcess> all = processRepo.findByEmployer_IdAndRequisition_Id(employer.getId(), requisitionId);

        return RequisitionPipelineResponse.builder()
                .requisitionId(requisition.getId())
                .jobRole(requisition.getJobRole())
                .openings(requisition.getOpenings())
                .location(requisition.getLocation().name())
                .employmentType(requisition.getEmploymentType().name())
                .status(requisition.getStatus().name())

                .shortlisted(filterByStage(all, Stage.SHORTLISTED))
                .assessment(filterByStage(all, Stage.ASSESSMENT))
                .interview(filterByStage(all, Stage.INTERVIEW))
                .offered(filterByStage(all, Stage.OFFERED))
                .offerAccepted(filterByStage(all, Stage.OFFER_ACCEPTED))
                .offerRejected(filterByStage(all, Stage.OFFER_REJECTED))
                .rejected(filterByStage(all, Stage.REJECTED))

                .build();
    }

    private void dropCandidateFromOtherEmployers(String profileId, Long acceptedEmployerId) {

        List<EmployerCandidateProcess> all = processRepo.findByProfileId(profileId);

        for (EmployerCandidateProcess process : all) {
            Long employerId = process.getEmployer().getId();

            if (!Objects.equals(employerId, acceptedEmployerId)
                    && process.getStage() != Stage.DROPPED
                    && process.getStage() != Stage.OFFER_ACCEPTED) {

                process.setStage(Stage.DROPPED);
                processRepo.save(process);

                // ✅ email to other employer
                try {
                   // emailService.candidateDroppedEmailToEmployer(process.getEmployer().getEmail(), profileId);
                } catch (Exception ignored) {}
            }
        }
    }

    private List<PipelineCandidateResponse> filterByStage(List<EmployerCandidateProcess> all, Stage stage) {
        return all.stream()
                .filter(p -> p.getStage() == stage)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PipelineCandidateResponse mapToResponse(EmployerCandidateProcess p) {
        return PipelineCandidateResponse.builder()
                .profileId(p.getProfileId())
                .stage(p.getStage().name())
                .assessmentLink(p.getAssessmentLink())
                .interviewLink(p.getInterviewLink())
                .offerLetterLink(p.getOfferLetterLink())
                .technicalInterviewerEmail(p.getTechnicalInterviewerEmail())
                .build();
    }
}
