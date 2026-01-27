package com.alphonso.moodle_employer_service.Employer.ServiceImp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Employer.Entity.Employer;
import com.alphonso.moodle_employer_service.Employer.Entity.Requisition;
import com.alphonso.moodle_employer_service.Employer.Entity.RequisitionSkill;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.CandidateProfileViewException;
import com.alphonso.moodle_employer_service.Employer.OpenFeign.InterviewerClient;
import com.alphonso.moodle_employer_service.Employer.OpenFeign.ProfileClient;
import com.alphonso.moodle_employer_service.Employer.Repository.EmployerRepository;
import com.alphonso.moodle_employer_service.Employer.Repository.RequisitionRepository;
import com.alphonso.moodle_employer_service.Employer.Repository.RequisitionSkillRepository;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.CandidateProfileDTO;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.CreateRequisitionRequest;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateFullProfileViewResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateMatchResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateProfileLeftViewResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.InterviewResultResponse;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.RequisitionResponse;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.AssessmentResultResponse;
import com.alphonso.moodle_employer_service.Moodle.Service.AssessmentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository employerRepo;
    private final RequisitionRepository requisitionRepo;
    private final RequisitionSkillRepository skillRepo;
    private final ProfileClient profileClient;
    private final InterviewerClient interviewerClient;
    private final AssessmentReportService assessmentReportService;

    @Transactional
    public RequisitionResponse createRequisition(String employerEmail, CreateRequisitionRequest req) {

        Employer employer = employerRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        Requisition requisition = new Requisition();
        requisition.setEmployer(employer);
        requisition.setJobRole(req.getJobRole());
        requisition.setExperienceRequired(req.getExperienceRequired());

        requisition.setEmploymentType(
                Requisition.EmploymentType.valueOf(req.getEmploymentType())
        );

        requisition.setLocation(
                Requisition.Location.valueOf(req.getLocation())
        );

        requisition.setOpenings(req.getOpenings());
        requisition.setJobDescription(req.getJobDescription());

        if (req.getRequiredSkillIds() != null && !req.getRequiredSkillIds().isEmpty()) {
            requisition.getRequiredSkills()
                    .addAll(skillRepo.findByIdIn(req.getRequiredSkillIds()));
        }

        if (req.getNiceSkillIds() != null && !req.getNiceSkillIds().isEmpty()) {
            requisition.getNiceToHaveSkills()
                    .addAll(skillRepo.findByIdIn(req.getNiceSkillIds()));
        }

        Requisition saved = requisitionRepo.save(requisition);

        return new RequisitionResponse(
                saved.getJobRole(),
                saved.getExperienceRequired(),
                saved.getEmploymentType().name(),
                saved.getLocation().name(),
                saved.getOpenings(),
                saved.getStatus().name(),
                saved.getJobDescription(),
                saved.getRequiredSkills()
                        .stream()
                        .map(RequisitionSkill::getName)
                        .collect(Collectors.toSet())
        );
    }
    
    @Transactional(readOnly = true)
    public List<CandidateMatchResponse> matchCandidates(Long requisitionId, String employerEmail) {

        try {
            Requisition requisition = requisitionRepo.findById(requisitionId)
                    .orElseThrow(() -> new IllegalArgumentException("Requisition not found"));

            if (!requisition.getEmployer().getEmail().equals(employerEmail)) {
                throw new SecurityException("Unauthorized access to requisition");
            }

            if (requisition.getStatus() != Requisition.Status.ACTIVE) {
                throw new IllegalStateException("Requisition is not active");
            }

            Integer  jobOpenings = requisition.getOpenings();
            if (jobOpenings == null || jobOpenings <= 0 || jobOpenings > 20) {
                throw new IllegalArgumentException("Job openings must be between 1 and 20");
            }

            if (requisition.getRequiredSkills() == null || requisition.getRequiredSkills().isEmpty()) {
                throw new IllegalStateException("Requisition must have required skills");
            }

            Set<String> requiredSkills = requisition.getRequiredSkills()
                    .stream()
                    .map(skill -> skill.getName().toLowerCase())
                    .collect(Collectors.toSet());

            int fetchLimit = jobOpenings + 10;

            List<CandidateProfileDTO> candidates;
            try {
                candidates = profileClient.getCandidatesByRoleAndStatus(
                        requisition.getJobRole(),
                        "COMPLETED_T1",
                        fetchLimit
                );
            } catch (Exception ex) {
                throw new RuntimeException("Candidate service unavailable", ex);
            }

            if (candidates == null || candidates.isEmpty()) {
                return Collections.emptyList();
            }

            List<CandidateMatchResponse> bucket90 = new ArrayList<>();
            List<CandidateMatchResponse> bucket80 = new ArrayList<>();
            List<CandidateMatchResponse> bucket70 = new ArrayList<>();
            List<CandidateMatchResponse> bucket60 = new ArrayList<>();

            for (CandidateProfileDTO candidate : candidates) {

                if (candidate.getCoreSkills() == null || candidate.getCoreSkills().isEmpty()) {
                    continue;
                }

                Set<String> candidateSkills = candidate.getCoreSkills()
                        .stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                long matchedCount = requiredSkills.stream()
                        .filter(candidateSkills::contains)
                        .count();

                double matchPercentage = (matchedCount * 100.0) / requiredSkills.size();
                double rounded = Math.round(matchPercentage * 100.0) / 100.0;

                if (rounded < 60) {
                    continue;
                }

                CandidateMatchResponse response = new CandidateMatchResponse(
                        candidate.getProfileId(),
                        candidate.getName(),
                        candidate.getRole(),
                        candidate.getTotalExperience(),
                        rounded,
                        candidate.getEmail()
                );

                if (rounded >= 90) {
                    bucket90.add(response);
                } else if (rounded >= 80) {
                    bucket80.add(response);
                } else if (rounded >= 70) {
                    bucket70.add(response);
                } else {
                    bucket60.add(response);
                }
            }

            Comparator<CandidateMatchResponse> byMatchDesc =
                    Comparator.comparing(CandidateMatchResponse::getSkillMatchPercentage).reversed();

            bucket90.sort(byMatchDesc);
            bucket80.sort(byMatchDesc);
            bucket70.sort(byMatchDesc);
            bucket60.sort(byMatchDesc);

            List<CandidateMatchResponse> finalList = new ArrayList<>();
            List<Iterator<CandidateMatchResponse>> cycle = Arrays.asList(
                    bucket90.iterator(),
                    bucket80.iterator(),
                    bucket70.iterator(),
                    bucket60.iterator()
            );

            boolean added;
            do {
                added = false;
                for (Iterator<CandidateMatchResponse> it : cycle) {
                    if (it.hasNext()) {
                        finalList.add(it.next());
                        added = true;

                        if (finalList.size() == fetchLimit) {
                            return finalList;
                        }
                    }
                }
            } while (added);

            return finalList;

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error while matching candidates", ex);
        }
    }

    @Transactional(readOnly = true)
    public CandidateFullProfileViewResponse getCandidateFullProfileView(String profileId) {

        try {
            CandidateProfileLeftViewResponse profileResponse;
            try {
                ResponseEntity<CandidateProfileLeftViewResponse> profileEntity =
                        profileClient.getCandidateLeftPanel(profileId);

                profileResponse = profileEntity.getBody();

                if (profileResponse == null) {
                    throw new CandidateProfileViewException("Profile-service returned empty response for: {}" + profileId);
                }

            } catch (Exception ex) {
                throw new CandidateProfileViewException("Failed to fetch profile details from Profile-Service, "+ ex);
            }

            AssessmentResultResponse assessmentResponse;
            try {
                assessmentResponse = assessmentReportService.getAssessmentResult(profileId);
            } catch (Exception ex) {
                
                assessmentResponse = AssessmentResultResponse.builder()
                        .profileId(profileId)
                        .moodlePercentage(0.0)
                        .categoryScores(null)
                        .build();
            }

            InterviewResultResponse interviewResponse;
            try {
                ResponseEntity<InterviewResultResponse> interviewEntity =
                        interviewerClient.getInterviewResultByProfileId(profileId);

                interviewResponse = interviewEntity.getBody();

                if (interviewResponse == null) {
                    interviewResponse = InterviewResultResponse.builder()
                            .candidateProfileId(profileId)
                            .interviewPercentage(0.0)
                            .adaptabilityPercentage(0.0)
                            .analyticalPercentage(0.0)
                            .collaborationPercentage(0.0)
                            .communicationPercentage(0.0)
                            .designPercentage(0.0)
                            .executionPercentage(0.0)
                            .technicalPercentage(0.0)
                            .build();
                }

            } catch (Exception ex) {
                interviewResponse = InterviewResultResponse.builder()
                        .candidateProfileId(profileId)
                        .interviewPercentage(0.0)
                        .adaptabilityPercentage(0.0)
                        .analyticalPercentage(0.0)
                        .collaborationPercentage(0.0)
                        .communicationPercentage(0.0)
                        .designPercentage(0.0)
                        .executionPercentage(0.0)
                        .technicalPercentage(0.0)
                        .build();
            }

            Double moodlePercentage = (assessmentResponse != null && assessmentResponse.getMoodlePercentage() != null)
                    ? assessmentResponse.getMoodlePercentage()
                    : 0.0;

            Double interviewPercentage = (interviewResponse != null && interviewResponse.getInterviewPercentage() != null)
                    ? interviewResponse.getInterviewPercentage()
                    : 0.0;

            Double overAllPercentage1 = Math.round(((moodlePercentage + interviewPercentage) / 2.0) * 100.0) / 100.0;

            return CandidateFullProfileViewResponse.builder()
                    .profile(profileResponse)
                    .assessment(assessmentResponse)
                    .overAllPercentage(overAllPercentage1)
                    .interview(interviewResponse)
                    .build();

        } catch (CandidateProfileViewException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CandidateProfileViewException("Error while preparing candidate full profile view, "+ ex);
        }
    }
    
   // add the matching profiles to shortlist
    
}
