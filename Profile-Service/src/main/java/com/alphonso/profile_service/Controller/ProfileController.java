package com.alphonso.profile_service.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alphonso.profile_service.RequestDTO.ApplicationQuestionRequest;
import com.alphonso.profile_service.RequestDTO.EducationRequest;
import com.alphonso.profile_service.RequestDTO.ExperienceRequest;
import com.alphonso.profile_service.RequestDTO.PersonalInfoRequest;
import com.alphonso.profile_service.RequestDTO.SendOtpRequest;
import com.alphonso.profile_service.RequestDTO.SkillSelectionRequest;
import com.alphonso.profile_service.RequestDTO.VerifyOtpRequest;
import com.alphonso.profile_service.RequestDTO.VoluntaryDisclosureRequest;
import com.alphonso.profile_service.ResponseDTO.ApplicationQuestionResponse;
import com.alphonso.profile_service.ResponseDTO.CandidateDetailsForInterview;
import com.alphonso.profile_service.ResponseDTO.CandidateProfileDTO;
import com.alphonso.profile_service.ResponseDTO.CandidateProfileLeftViewResponse;
import com.alphonso.profile_service.ResponseDTO.EducationResponse;
import com.alphonso.profile_service.ResponseDTO.ExperienceResponse;
import com.alphonso.profile_service.ResponseDTO.InterviewerDTO;
import com.alphonso.profile_service.ResponseDTO.PersonalInfoResponse;
import com.alphonso.profile_service.ResponseDTO.ProfileDTO;
import com.alphonso.profile_service.ResponseDTO.ProfileSkillResponse;
import com.alphonso.profile_service.ResponseDTO.UserDTO;
import com.alphonso.profile_service.Service.IOtpService;
import com.alphonso.profile_service.Service.IProfileService;
import com.alphonso.profile_service.Repository.SkillGroupRepository;
import com.alphonso.profile_service.Repository.SkillRoleRepository;
import com.alphonso.profile_service.Repository.CoreSkillRepository;
import com.alphonso.profile_service.Repository.AdditionalSkillRepository;
import com.alphonso.profile_service.Repository.ProgrammingSkillRepository;
import com.alphonso.profile_service.Entity.SkillGroup;
import com.alphonso.profile_service.Entity.SkillRole;
import com.alphonso.profile_service.Entity.CoreSkills;
import com.alphonso.profile_service.Entity.AdditionalSkill;
import com.alphonso.profile_service.Entity.ProgrammingSkill;
import com.alphonso.profile_service.ResponseDTO.SkillGroupResponse;
import com.alphonso.profile_service.ResponseDTO.SkillRoleResponse;
import com.alphonso.profile_service.ResponseDTO.CoreSkillResponse;
import com.alphonso.profile_service.ResponseDTO.AdditionalSkillResponse;
import com.alphonso.profile_service.ResponseDTO.ProgrammingSkillResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@Validated
public class ProfileController {

	private final IProfileService profileService;
	private final IOtpService otpService;
	private final SkillGroupRepository skillGroupRepository;
	private final SkillRoleRepository skillRoleRepository;
	private final CoreSkillRepository coreSkillRepository;
	private final AdditionalSkillRepository additionalSkillRepository;
	private final ProgrammingSkillRepository programmingSkillRepository;

	public ProfileController(IProfileService userService, IOtpService otpService,
			SkillGroupRepository skillGroupRepository,
			SkillRoleRepository skillRoleRepository,
			CoreSkillRepository coreSkillRepository,
			AdditionalSkillRepository additionalSkillRepository,
			ProgrammingSkillRepository programmingSkillRepository) {
		this.profileService = userService;
		this.otpService = otpService;
		this.skillGroupRepository = skillGroupRepository;
		this.skillRoleRepository = skillRoleRepository;
		this.coreSkillRepository = coreSkillRepository;
		this.additionalSkillRepository = additionalSkillRepository;
		this.programmingSkillRepository = programmingSkillRepository;
	}

	@GetMapping("/draft")
	public ResponseEntity<ProfileDTO> getDraft(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles) {

		log.info("Fetching draft profile for user: {}", email);

		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);

		ProfileDTO profile = profileService.getOrCreateDraft(user.getId(), user.getEmail());
		log.info("Draft profile fetched successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/")
	public ResponseEntity<ProfileDTO> getProfile(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles) {
		log.info("Fetching draft profile for user: {}", email);

		System.out.println(userId + "," + email + "," + roles);

		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);

		ProfileDTO profile = profileService.getOrCreateProfile(user);
		log.info("Personal profile fetched successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/personal")
	public ResponseEntity<ProfileDTO> savePersonal(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody PersonalInfoRequest req) {

		log.info("Saving personal information");

		System.out.println(userId + "," + email + "," + roles);
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);

		log.debug("Saving personal info for user: {}", user.getEmail());
		ProfileDTO profile = profileService.savePersonal(user, req);
		log.info("Personal info saved successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/education")
	public ResponseEntity<ProfileDTO> addEducation(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody List<EducationRequest> req) {

		log.info("Adding education details");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Adding education for user: {}", user.getEmail());
		ProfileDTO profile = profileService.addEducation(user.getId(), user.getEmail(), req);
		log.info("Education details added successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/experience")
	public ResponseEntity<ProfileDTO> addExperience(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody ExperienceRequest req) {

		log.info("Adding experience details");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Adding experience for user: {}", user.getEmail());
		ProfileDTO profile = profileService.addExperience(user.getId(), user.getEmail(), user.getRole(), req);
		log.info("Experience details added successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/application-questions")
	public ResponseEntity<ProfileDTO> saveApplicationQuestions(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody ApplicationQuestionRequest req) {

		log.info("Saving application questions");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Saving application questions for user: {}", user.getEmail());
		ProfileDTO profile = profileService.saveApplicationQuestions(user.getId(), user.getEmail(), req);
		log.info("Application questions saved successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/voluntary-disclosures")
	public ResponseEntity<ProfileDTO> saveVoluntaryDisclosures(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody VoluntaryDisclosureRequest req) {

		log.info("Saving voluntary disclosures");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Saving voluntary disclosures for user: {}", user.getEmail());
		ProfileDTO profile = profileService.saveVoluntaryDisclosures(user.getId(), user.getEmail(), req);
		log.info("Voluntary disclosures saved successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/skills")
	public ResponseEntity<ProfileDTO> saveSkills(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody SkillSelectionRequest req) {

		log.info("Saving profile skills");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Saving skills for user: {}", user.getEmail());
		ProfileDTO profile = profileService.saveProfileSkills(user.getId(), user.getEmail(), req);
		log.info("Profile skills saved successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-Email") String email,
			@RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles, @Valid @RequestBody SendOtpRequest req) {
		log.info("Received request to send OTP to email: {}", req.getEmail());
		try {
			otpService.createAndSendOtp(req.getEmail(), roles);
			log.info("OTP sent successfully to: {}", req.getEmail());
			return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			log.warn("Failed to send OTP to {}: {}", req.getEmail(), ex.getMessage());
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception e) {
			log.error("Unexpected error while sending OTP to {}: {}", req.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to send OTP: " + e.getMessage()));
		}
	}

	@PostMapping("/verify-university-email")
	public ResponseEntity<?> verifyUniversityEmail(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles,
			@Valid @RequestBody VerifyOtpRequest req) {
		log.info("Verifying OTP for university email: {}", req.getEmail());
		try {
			profileService.verifyUniversityEmailOtp(req.getEmail(), req.getOtp());
			log.info("OTP verified successfully for email: {}", req.getEmail());
			return ResponseEntity.ok(Map.of("message", "OTP Verified"));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			log.warn("OTP verification failed for {}: {}", req.getEmail(), ex.getMessage());
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception e) {
			log.error("Unexpected error while verifying OTP for {}: {}", req.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to verify OTP: " + e.getMessage()));
		}
	}

	@PostMapping("/submit")
	public ResponseEntity<ProfileDTO> submitProfile(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles) {
		log.info("Submitting profile");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Submitting profile for user: {}", user.getEmail());
		ProfileDTO profile = profileService.submitProfile(user.getId(), user.getEmail(), user.getRole());
		log.info("Profile submitted successfully for user: {}", user.getEmail());
		return ResponseEntity.ok(profile);
	}

	@GetMapping("/")
	public ResponseEntity<ProfileDTO> getProfileDetails(@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-Email") String email, @RequestHeader("X-First-Name") String firstName,
			@RequestHeader(value = "X-Roles", required = false) String roles) {
		log.info("Fetching full profile");
		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setRole(roles);
		log.debug("Fetching profile for user: {}", user.getEmail());
		ProfileDTO profile = profileService.getProfileDetails(user.getEmail(), user.getRole());
		
		// For new users, profile will be null - return 200 with null data
		if (profile == null) {
			log.info("No profile found for new user: {} - returning null", user.getEmail());
			return ResponseEntity.ok(null);
		}
		
		log.info("Profile fetched successfully for user: {}", user.getEmail());
		if (user.getRole() != null && user.getRole().contains("INTERVIEWER")) {
			profile.setSkills(null);
		}

		return ResponseEntity.ok(profile);
	}

	@GetMapping("/personal")
	public ResponseEntity<PersonalInfoResponse> getPersonalInfo(@RequestHeader("X-Email") String email) {
		log.info("Fetching personal info for user: {}", email);
		PersonalInfoResponse response = profileService.getPersonalInfo(email);
		log.info("Personal info fetched successfully for user: {}", email);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/education")
	public ResponseEntity<List<EducationResponse>> getEducation(@RequestHeader("X-Email") String email) {
		log.info("Fetching education details for user: {}", email);
		List<EducationResponse> response = profileService.getEducation(email);
		log.info("Education details fetched successfully for user: {}, count={}", email,
				response != null ? response.size() : 0);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/experience")
	public ResponseEntity<List<ExperienceResponse>> getExperience(@RequestHeader("X-Email") String email) {
		log.info("Fetching experience details for user: {}", email);
		List<ExperienceResponse> response = profileService.getExperience(email);
		log.info("Experience details fetched successfully for user: {}, count={}", email,
				response != null ? response.size() : 0);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skills")
	public ResponseEntity<ProfileSkillResponse> getSkills(@RequestHeader("X-Email") String email) {
		log.info("Fetching profile skills for user: {}", email);
		ProfileSkillResponse response = profileService.getProfileSkills(email);
		log.info("Profile skills fetched successfully for user: {}", email);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/questions")
	public ResponseEntity<ApplicationQuestionResponse> getQuestions(@RequestHeader("X-Email") String email) {
		log.info("Fetching application questions for user: {}", email);
		ApplicationQuestionResponse response = profileService.getApplicationQuestions(email);
		log.info("Application questions fetched successfully for user: {}", email);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/voluntary")
	public ResponseEntity<VoluntaryDisclosureRequest> getVoluntary(@RequestHeader("X-Email") String email) {
		log.info("Fetching voluntary disclosure for user: {}", email);
		VoluntaryDisclosureRequest response = profileService.getVoluntaryDisclosure(email);
		log.info("Voluntary disclosure fetched successfully for user: {}", email);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/by-email")
	public InterviewerDTO getByEmail(@RequestParam("email") String email) {
		log.info("Fetching profile by email: {}", email);
		InterviewerDTO response = profileService.getProfileByEmail(email);
		log.info("Profile fetched by email successfully: {}", email);
		return response;
	}

	@GetMapping("/quiz-passed")
	public List<CandidateDetailsForInterview> getQuizPassedCandidates() {
		log.info("Fetching quiz passed candidates list");
		List<CandidateDetailsForInterview> response = profileService.getQuizPassedCandidates();
		log.info("Quiz passed candidates fetched successfully, count={}", response != null ? response.size() : 0);
		if (response == null) {
	        return Collections.emptyList();
	    }
		return response;
	}

	@PostMapping("/candidates/assessment-status")
	public void updateAssessmentStatus(String profileId, String status) {
		log.info("Updating assessment status. profileId={}, status={}", profileId, status);
		profileService.updateAssessmentStatus(profileId, status);
		log.info("Assessment status updated successfully. profileId={}, status={}", profileId, status);
	}

	@GetMapping("/search")
	public List<CandidateProfileDTO> getCandidatesByRoleAndStatus(
        @RequestParam String jobRole,
        @RequestParam String status,
        @RequestParam int limit
    ){
		  return profileService.getCandidatesByRoleAndStatus(jobRole, status, limit);
	}
	
	@GetMapping("/{profileId}")
	public ResponseEntity<CandidateProfileLeftViewResponse> getCandidateLeftPanel(@PathVariable String profileId) {
		log.info("Fetching candidate left panel view for profileId={}", profileId);
		CandidateProfileLeftViewResponse response = profileService.getCandidateLeftPanel(profileId);
		log.info("Candidate left panel fetched successfully for profileId={}", profileId);
		return ResponseEntity.ok(response);
	}

	// Skill Master Data Endpoints
	@GetMapping("/skill-master/groups")
	public ResponseEntity<List<SkillGroupResponse>> getSkillGroups() {
		log.info("Fetching all skill groups");
		List<SkillGroup> groups = skillGroupRepository.findAll();
		List<SkillGroupResponse> response = groups.stream()
			.map(group -> SkillGroupResponse.builder()
				.id(group.getId())
				.name(group.getGroupName())
				.build())
			.toList();
		log.info("Fetched {} skill groups", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skill-master/roles")
	public ResponseEntity<List<SkillRoleResponse>> getSkillRoles() {
		log.info("Fetching all skill roles");
		List<SkillRole> roles = skillRoleRepository.findAll();
		List<SkillRoleResponse> response = roles.stream()
			.map(role -> SkillRoleResponse.builder()
				.id(role.getId())
				.name(role.getRoleName())
				.groupId(role.getGroup() != null ? role.getGroup().getId() : null)
				.build())
			.toList();
		log.info("Fetched {} skill roles", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skill-master/core-skills")
	public ResponseEntity<List<CoreSkillResponse>> getCoreSkills() {
		log.info("Fetching all core skills");
		List<CoreSkills> coreSkills = coreSkillRepository.findAll();
		List<CoreSkillResponse> response = coreSkills.stream()
			.map(skill -> CoreSkillResponse.builder()
				.id(skill.getId())
				.name(skill.getSkillName())
				.build())
			.toList();
		log.info("Fetched {} core skills", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skill-master/additional-skills")
	public ResponseEntity<List<AdditionalSkillResponse>> getAdditionalSkills() {
		log.info("Fetching all additional skills");
		List<AdditionalSkill> additionalSkills = additionalSkillRepository.findAll();
		List<AdditionalSkillResponse> response = additionalSkills.stream()
			.map(skill -> AdditionalSkillResponse.builder()
				.id(skill.getId())
				.name(skill.getSkillName())
				.build())
			.toList();
		log.info("Fetched {} additional skills", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/skill-master/programming-skills")
	public ResponseEntity<List<ProgrammingSkillResponse>> getProgrammingSkills() {
		log.info("Fetching all programming skills");
		List<ProgrammingSkill> programmingSkills = programmingSkillRepository.findAll();
		List<ProgrammingSkillResponse> response = programmingSkills.stream()
			.map(skill -> ProgrammingSkillResponse.builder()
				.id(skill.getId())
				.name(skill.getProgramName())
				.build())
			.toList();
		log.info("Fetched {} programming skills", response.size());
		return ResponseEntity.ok(response);
	}
}
