package com.alphonso.moodle_employer_service.Employer.OpenFeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.InterviewResultResponse;
import com.alphonso.moodle_employer_service.Moodle.Security.FeignClientInterceptor;


@FeignClient(name = "Interviewer-Service", configuration = FeignClientInterceptor.class)
public interface InterviewerClient {

	 @GetMapping("/profile/{profileId}")
	    public ResponseEntity<InterviewResultResponse> getInterviewResultByProfileId(
	            @PathVariable("profileId") String profileId
	    );
}
