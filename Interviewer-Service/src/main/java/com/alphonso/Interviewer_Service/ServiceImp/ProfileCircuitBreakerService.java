package com.alphonso.Interviewer_Service.ServiceImp;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alphonso.Interviewer_Service.FeignClient.ProfileClient;
import com.alphonso.Interviewer_Service.ResponseDTO.ApiResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.CandidateDetailsForInterview;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCircuitBreakerService {

    private final ProfileClient profileClient;

    @CircuitBreaker(name = "profileServiceCB", fallbackMethod = "getByEmailFallback")
    @Retry(name = "profileServiceRetry")
    public InterviewerDto getByEmail(String email) {

        ApiResponse<InterviewerDto> apiResp = profileClient.getByEmail(email);

        if (apiResp == null || apiResp.getData() == null) {
            throw new RuntimeException("Profile service returned empty response for email: " + email);
        }

        return apiResp.getData();
    }

    public InterviewerDto getByEmailFallback(String email, Throwable ex) {
        log.error("Profile-Service DOWN getByEmail fallback, email={}, reason={}", email, ex.getMessage());
        return null; 
    }

    @CircuitBreaker(name = "profileServiceCB", fallbackMethod = "getQuizPassedCandidatesFallback")
    @Retry(name = "profileServiceRetry")
    public List<CandidateDetailsForInterview> getQuizPassedCandidates() {

        ApiResponse<List<CandidateDetailsForInterview>> apiResp = profileClient.getQuizPassedCandidates();

        List<CandidateDetailsForInterview> list = (apiResp != null) ? apiResp.getData() : null;

        return (list == null) ? Collections.emptyList() : list;
    }

    public List<CandidateDetailsForInterview> getQuizPassedCandidatesFallback(Throwable ex) {
        log.error("Profile-Service DOWN getQuizPassedCandidates fallback, reason={}", ex.getMessage());
        return Collections.emptyList();
    }

    @CircuitBreaker(name = "profileServiceCB", fallbackMethod = "updateAssessmentStatusFallback")
    @Retry(name = "profileServiceRetry")
    public void updateAssessmentStatus(String profileId, String status) {
        profileClient.updateAssessmentStatus(profileId, status);
    }

    public void updateAssessmentStatusFallback(String profileId, String status, Throwable ex) {
        log.error("Profile-Service DOWN updateAssessmentStatus fallback profileId={}, status={}, reason={}",
                profileId, status, ex.getMessage());
    }
}
