package com.alphonso.Interviewer_Service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alphonso.Interviewer_Service.Exception.InterviewerServiceException.BadRequestException;
import com.alphonso.Interviewer_Service.RequestDTO.FeedbackRequest;
import com.alphonso.Interviewer_Service.RequestDTO.MonthlyAvailabilityRequest;
import com.alphonso.Interviewer_Service.RequestDTO.RadarChartDto;
import com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDetailsResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.DateResult;
import com.alphonso.Interviewer_Service.ResponseDTO.FeedbackResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewResultResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewSummaryDto;
import com.alphonso.Interviewer_Service.Service.IInterviewerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviewer")
@RequiredArgsConstructor
@Slf4j
public class InterviewerController {

	private final IInterviewerService interviewerService;

	@PostMapping("/availability/month")
	public ResponseEntity<?> addMonthlyAvailability(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String callerEmail, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody MonthlyAvailabilityRequest req) {

		log.info("POST /availability/month called by {} with userId {}", callerEmail, userId);

		if (callerEmail == null || callerEmail.isBlank()) {
			log.warn("Missing X-Email header");
			throw new BadRequestException("Missing X-Email header");
		}

		List<DateResult> results = interviewerService.addMonthlyAvailability(req, callerEmail.trim().toLowerCase());

		if (results.isEmpty()) {
			log.info("All availability slots created successfully for {}", callerEmail);
			return ResponseEntity.ok(Map.of("message", "✅ No conflicts found. All slots created successfully"));
		}

		log.info("Returning {} conflict/invalid results for {}", results.size(), callerEmail);
		return ResponseEntity.ok(results);
	}

	@GetMapping("/availability/month")
	public ResponseEntity<List<AvailabilitySlotResponse>> getCurrentMonthAvailability(
			@RequestHeader("X-Email") String callerEmail) {
		log.info("GET /availability/month called for {}", callerEmail);

		List<AvailabilitySlotResponse> slots = interviewerService.getCurrentMonthAvailability(callerEmail);

		log.info("Returning {} slots for {}", slots.size(), callerEmail);

		return ResponseEntity.ok(slots);
	}

	@GetMapping("/todayInterviews")
	public ResponseEntity<List<InterviewSummaryDto>> getTodayInterviews(@RequestHeader("X-Email") String callerEmail) {

		log.info("GET /today interviews for {}", callerEmail);

		if (callerEmail == null || callerEmail.isBlank()) {
			log.warn("Missing X-Email header");
			throw new BadRequestException("Missing X-Email header");
		}

		List<InterviewSummaryDto> interviewResults = interviewerService.getTodayInterviews(callerEmail.trim().toLowerCase());

		if (interviewResults.isEmpty()) {
			log.info("No interviews found today for {}", callerEmail);
		} else {
			log.info("Found {} interviews today for {}", interviewResults.size(), callerEmail);
		}

		return ResponseEntity.ok(interviewResults);
	}

	@GetMapping("/upcomingInterviews")
	public ResponseEntity<List<InterviewSummaryDto>> getUpcomingInterviews(@RequestHeader("X-Email") String callerEmail) {

		log.info("GET /upcoming interviews for {}", callerEmail);

		if (callerEmail == null || callerEmail.isBlank()) {
			log.warn("Missing X-Email header");
			throw new BadRequestException("Missing X-Email header");
		}

		List<InterviewSummaryDto> upcoming = interviewerService.getUpcomingInterviews(callerEmail.trim().toLowerCase());

		if (upcoming.isEmpty()) {
			log.info("No upcoming interviews found for {}", callerEmail);
		} else {
			log.info("Found {} upcoming interviews for {}", upcoming.size(), callerEmail);
		}

		return ResponseEntity.ok(upcoming);
	}

	@PostMapping("/feedback/{interviewId}")
	public ResponseEntity<FeedbackResponse> createFeedback(@PathVariable String interviewId,
			@RequestBody FeedbackRequest req) {
		FeedbackResponse resp = interviewerService.saveOrUpdateFeedback(interviewId, req);
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/feedback/{profileId}")
	public ResponseEntity<List<FeedbackResponse>> forCandidate(@PathVariable String profileId) {
		return ResponseEntity.ok(interviewerService.getFeedbackForCandidate(profileId));
	}

	@GetMapping("/{feedbackId}/radar")
	public ResponseEntity<RadarChartDto> radar(@PathVariable Long feedbackId) {
		return ResponseEntity.ok(interviewerService.toRadarDto(feedbackId));
	}

	@GetMapping("/profile/{profileId}")
	public ResponseEntity<InterviewResultResponse> getInterviewResultByProfileId(
			@PathVariable("profileId") String profileId) {
		InterviewResultResponse response = interviewerService.getInterviewResult(profileId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skills")
	public ResponseEntity<List<com.alphonso.Interviewer_Service.Entity.SkillSetDetails>> getAllSkills() {
		log.info("GET /skills - Fetching all skills");
		List<com.alphonso.Interviewer_Service.Entity.SkillSetDetails> skills = interviewerService.getAllSkills();
		log.info("Returning {} skills", skills.size());
		return ResponseEntity.ok(skills);
	}

	@GetMapping("/details")
	public ResponseEntity<InterviewerDetailsResponse> getInterviewerDetails(
			@RequestHeader("X-Email") String callerEmail) {
		log.info("GET /details called for {}", callerEmail);

		if (callerEmail == null || callerEmail.isBlank()) {
			log.warn("Missing X-Email header");
			throw new BadRequestException("Missing X-Email header");
		}

		InterviewerDetailsResponse response = interviewerService.getInterviewerDetails(callerEmail.trim().toLowerCase());

		log.info("Returning interviewer details for {} with {} skills", 
				callerEmail, response.getSkills() != null ? response.getSkills().size() : 0);

		return ResponseEntity.ok(response);
	}
}