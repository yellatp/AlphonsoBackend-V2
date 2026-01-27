package com.alphonso.moodle_employer_service.Employer.OpenFeign;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.alphonso.moodle_employer_service.Employer.RequestDTO.CandidateProfileDTO;
import com.alphonso.moodle_employer_service.Employer.ResponseDTO.CandidateProfileLeftViewResponse;
import com.alphonso.moodle_employer_service.Moodle.Security.FeignClientInterceptor;

@FeignClient(name = "Profile-Service", configuration = FeignClientInterceptor.class)
public interface ProfileClient {

	 @GetMapping("/api/candidates/search")
	    List<CandidateProfileDTO> getCandidatesByRoleAndStatus(
	        @RequestParam String jobRole,
	        @RequestParam String status,
	        @RequestParam int limit
	    );

		@GetMapping("/{profileId}")
		public ResponseEntity<CandidateProfileLeftViewResponse> getCandidateLeftPanel(@PathVariable String profileId);
}