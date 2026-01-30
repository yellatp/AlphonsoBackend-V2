package com.alphonso.Interviewer_Service.FeignClient;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.alphonso.Interviewer_Service.ResponseDTO.ApiResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.CandidateDetailsForInterview;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDto;
import com.alphonso.Interviewer_Service.Security.FeignClientInterceptor;

@FeignClient(name = "Profile-Service", configuration = FeignClientInterceptor.class)
public interface ProfileClient {

    @GetMapping("/api/profile/by-email")
    ApiResponse<InterviewerDto> getByEmail(@RequestParam("email") String email);
    
    @GetMapping("/api/profile/quiz-passed")
    ApiResponse<List<CandidateDetailsForInterview>> getQuizPassedCandidates();

    @PostMapping("/api/profile/candidates/assessment-status")
    void updateAssessmentStatus(@RequestParam("profileId") String profileId,
                                @RequestParam("status") String status);
}