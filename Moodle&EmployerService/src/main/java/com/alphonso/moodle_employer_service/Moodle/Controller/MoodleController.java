package com.alphonso.moodle_employer_service.Moodle.Controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.MoodleRequest;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.ProfileResultResponse;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.SyncResponse;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.AssessmentResultResponse;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.UserAssessmentReportDto;
import com.alphonso.moodle_employer_service.Moodle.Service.AssessmentReportService;
import com.alphonso.moodle_employer_service.Moodle.Service.MoodleApiService;
import com.alphonso.moodle_employer_service.Moodle.Service.MoodleUserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moodle")
public class MoodleController {

	private final MoodleUserSyncService moodleUserService;
	private final MoodleApiService moodleAPI;
	private final AssessmentReportService reportService;


	@PostMapping("/sync-user")
	public SyncResponse syncUserToMoodle(@RequestBody MoodleRequest dto) {
		return moodleUserService.createUserAndLinkSkills(dto);
	}

	@PostMapping("/quiz")
	public ResponseEntity<Map<String, Object>> createQuiz(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles) {

		log.info("✅ Launching assessment for email: {}", email);

		String quizUrl = moodleAPI.launchUserQuiz(email);

		Map<String, Object> result = new HashMap<>();
		result.put("launchUrl", quizUrl);

		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PostMapping("/sync-profile-results")
	public ProfileResultResponse syncUserAttemptsUsingFeign(@RequestParam String profileId) {
		return moodleAPI.syncProfileResultsProfiles(profileId);

	}

	@GetMapping("/candidateReport")
	public ResponseEntity<UserAssessmentReportDto> getUserReport(@RequestHeader("X-Email") String email) {
		UserAssessmentReportDto dto = reportService.getUserReport(email);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/getAllReports")
	public ResponseEntity<Page<UserAssessmentReportDto>> getAllReports(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(required = false) Long sinceEpochSeconds) {

		LocalDateTime since = null;
		if (sinceEpochSeconds != null) {
			since = LocalDateTime.ofInstant(Instant.ofEpochSecond(sinceEpochSeconds), ZoneOffset.UTC);
		}

		Page<UserAssessmentReportDto> p = reportService.getAllReports(PageRequest.of(page, size), since);
		return ResponseEntity.ok(p);
	}

	@GetMapping("/profile/{profileId}")
	public ResponseEntity<AssessmentResultResponse> getAssessmentResultByProfile(@PathVariable String profileId) {
		AssessmentResultResponse response = reportService.getAssessmentResult(profileId);
		return ResponseEntity.ok(response);
	}
}