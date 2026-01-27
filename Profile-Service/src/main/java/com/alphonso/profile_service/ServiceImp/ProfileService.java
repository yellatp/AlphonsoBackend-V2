package com.alphonso.profile_service.ServiceImp;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.alphonso.profile_service.Entity.AdditionalSkill;
import com.alphonso.profile_service.Entity.Address;
import com.alphonso.profile_service.Entity.ApplicationQuestions;
import com.alphonso.profile_service.Entity.CoreSkills;
import com.alphonso.profile_service.Entity.Education;
import com.alphonso.profile_service.Entity.Experience;
import com.alphonso.profile_service.Entity.JobDetail;
import com.alphonso.profile_service.Entity.Portfolio;
import com.alphonso.profile_service.Entity.ProfileDetails;
import com.alphonso.profile_service.Entity.ProfileDetails.AssessmentStatus;
import com.alphonso.profile_service.Entity.ProfileSkills;
import com.alphonso.profile_service.Entity.ProgrammingSkill;
import com.alphonso.profile_service.Entity.SkillGroup;
import com.alphonso.profile_service.Entity.SkillRole;
import com.alphonso.profile_service.Entity.VoluntaryDisclosure;
import com.alphonso.profile_service.Exception.ProfileServiceException.BadRequestException;
import com.alphonso.profile_service.Exception.ProfileServiceException.ConflictException;
import com.alphonso.profile_service.Exception.ProfileServiceException.NotFoundException;
import com.alphonso.profile_service.Exception.ProfileServiceException.ProfileNotFoundException;
import com.alphonso.profile_service.Exception.ProfileServiceException.UserNotFoundException;
import com.alphonso.profile_service.Repository.AdditionalSkillRepository;
import com.alphonso.profile_service.Repository.ApplicationQuestionRepository;
import com.alphonso.profile_service.Repository.CoreSkillRepository;
import com.alphonso.profile_service.Repository.EducationRepository;
import com.alphonso.profile_service.Repository.ExperienceRepository;
import com.alphonso.profile_service.Repository.ProfileRepository;
import com.alphonso.profile_service.Repository.ProfileSkillsRepository;
import com.alphonso.profile_service.Repository.ProgrammingSkillRepository;
import com.alphonso.profile_service.Repository.SkillGroupRepository;
import com.alphonso.profile_service.Repository.SkillRoleRepository;
import com.alphonso.profile_service.Repository.VoluntaryDisclosureRepository;
import com.alphonso.profile_service.RequestDTO.ApplicationQuestionRequest;
import com.alphonso.profile_service.RequestDTO.EducationRequest;
import com.alphonso.profile_service.RequestDTO.ExperienceRequest;
import com.alphonso.profile_service.RequestDTO.PersonalInfoRequest;
import com.alphonso.profile_service.RequestDTO.SkillSelectionRequest;
import com.alphonso.profile_service.RequestDTO.VoluntaryDisclosureRequest;
import com.alphonso.profile_service.ResponseDTO.ApplicationQuestionResponse;
import com.alphonso.profile_service.ResponseDTO.CandidateDetailsForInterview;
import com.alphonso.profile_service.ResponseDTO.CandidateProfileDTO;
import com.alphonso.profile_service.ResponseDTO.CandidateProfileLeftViewResponse;
import com.alphonso.profile_service.ResponseDTO.EducationResponse;
import com.alphonso.profile_service.ResponseDTO.ExperienceLeftViewResponse;
import com.alphonso.profile_service.ResponseDTO.ExperienceResponse;
import com.alphonso.profile_service.ResponseDTO.InterviewerDTO;
import com.alphonso.profile_service.ResponseDTO.JobDetailResponse;
import com.alphonso.profile_service.ResponseDTO.PersonalInfoResponse;
import com.alphonso.profile_service.ResponseDTO.ProfileDTO;
import com.alphonso.profile_service.ResponseDTO.ProfileSkillResponse;
import com.alphonso.profile_service.ResponseDTO.UserDTO;
import com.alphonso.profile_service.Service.IEmailService;
import com.alphonso.profile_service.Service.IProfileService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService implements IProfileService {

	private final ProfileRepository profileRepo;
	private final ApplicationQuestionRepository appQuestionRepo;
	private final VoluntaryDisclosureRepository voluntaryDisclosureRepo;
	private final SkillGroupRepository groupRepo;
	private final SkillRoleRepository roleRepo;
	private final CoreSkillRepository coreRepo;
	private final AdditionalSkillRepository addRepo;
	private final ProfileSkillsRepository profileSkillRepo;
	//private final UserServiceClient userClient;
	private final OtpService otpService;
	private final IEmailService emailService;
	private final ProgrammingSkillRepository programmingRepo;
	private final EducationRepository educationRepo;
	private final ExperienceRepository experienceRepo;

//	private Long getCurrentUserId(String email) {
//		log.info("Fetching user ID from user service for email: {}", email);
//		try {
//			UserDTO user = userClient.getUserByEmail(email);
//			Long userId = (user != null) ? user.getId() : null;
//			if (userId == null) {
//				log.warn("No user found for email: {}", email);
//			} else {
//				log.debug("User ID fetched successfully for email: {}", email);
//			}
//			return userId;
//		} catch (Exception ex) {
//			log.error("Failed to fetch user from user-service for {}: {}", email, ex.getMessage(), ex);
//			throw new RuntimeException("Failed to fetch user from user-service: " + ex.getMessage(), ex);
//		}
//	}

	public InterviewerDTO getProfileByEmail(String email) {

		ProfileRepository.ProfileProjection projection = profileRepo.findProjectedByEmail(email)
				.orElseThrow(() -> new RuntimeException("Profile not found with email: " + email));

		InterviewerDTO dto = new InterviewerDTO();
		dto.setProfileId(projection.getProfileId());
		dto.setEmail(projection.getEmail());
		dto.setFirstName(projection.getFirstName());
		dto.setLastName(projection.getLastName());

		return dto;
	}

	@Transactional
	public ProfileDTO getOrCreateDraft(Long userId, String email) {
		log.info("Fetching or creating draft profile for email: {}", email);
		//Long userId = getCurrentUserId(email);
		ProfileDetails profile = profileRepo.findByUserId(userId).orElseGet(() -> {
			log.debug("No draft found for {}, creating new profile draft", email);
			ProfileDetails p = ProfileDetails.builder().userId(userId).email(email).status(ProfileDetails.Status.DRAFT)
					.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
			return profileRepo.save(p);
		});
		log.info("Draft profile ready for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO getOrCreateProfile(UserDTO user) {
		log.info("Fetching or creating profile for user: {}", user.getEmail());
		Optional<ProfileDetails> existingOpt = profileRepo.findByUserId(user.getId());

		ProfileDetails profile;
		if (existingOpt.isPresent()) {
			log.debug("Existing profile found for {}", user.getEmail());
			profile = existingOpt.get();
		} else {
			log.debug("Creating new profile for {}", user.getEmail());
			profile = ProfileDetails.builder().userId(user.getId()).firstName(user.getFirstName())
					.email(user.getEmail()).status(ProfileDetails.Status.DRAFT).createdAt(LocalDateTime.now()).build();

			profile = profileRepo.save(profile);
		}
		log.info("Profile ready for {}", user.getEmail());
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO getProfileDetails(String email, String role) {
		log.info("Fetching profile for user: {}", email);

		// For new users who haven't created a profile yet, return null instead of throwing 404
		// This allows the frontend to handle new users gracefully
		return profileRepo.findByEmail(email).map(this::toDto).orElse(null);
	}

	@Transactional
	public ProfileDTO savePersonal(UserDTO user, PersonalInfoRequest req) {
		log.info("Saving personal details for user: {}", user.getEmail());
		//Long userId = getCurrentUserId(user.getEmail());

		ProfileDetails profile = profileRepo.findByUserId(user.getId()).orElseGet(() -> {
			log.debug("No existing profile found, creating new for {}", user.getEmail());
			ProfileDetails newProfile = new ProfileDetails();
			newProfile.setUserId(user.getId());
			newProfile.setStatus(ProfileDetails.Status.DRAFT);
			return newProfile;
		});

		if (profile.getStatus() == ProfileDetails.Status.SUBMITTED) {
			log.warn("Attempt to modify submitted profile for {}", user.getEmail());
			throw new ConflictException("Profile already submitted");
		}
		if (req.getUniversityEmail() != null && !req.getUniversityEmail().isBlank()) {
			String universityEmail = req.getUniversityEmail().toLowerCase();

			boolean isVerified = otpService.isVerified(universityEmail);
			if (!isVerified) {
				log.warn("University email not verified for {}", universityEmail);
				throw new BadRequestException("University email not verified. Please verify before saving.");
			}

			boolean isExisitedUniversityEmail = otpService.isAlreadyExist(universityEmail);
			if (!isExisitedUniversityEmail) {
				log.warn("University email already existed: {}", universityEmail);
				throw new BadRequestException("University email is already existed in our Database.");
			}

			profile.setUniversityEmail(universityEmail);
			profile.setUniversityEmailVerified(true);
		}

		profile.setFirstName(req.getFirstName());
		profile.setLastName(req.getLastName());
		profile.setPhoneNumber(req.getPhoneNumber());
		profile.setEmail(user.getEmail());
		profile.setUpdatedAt(LocalDateTime.now());

		Address address = new Address(req.getAddress(), req.getCity(), req.getState(), req.getCountry(),
				req.getPincode());
		profile.setAddress(address);

		Portfolio portfolio = new Portfolio(req.getLinkedIn(), req.getGitHub(), req.getPorfolioId(), req.getOthers());
		profile.setPortfolio(portfolio);

		profile.setStatus(ProfileDetails.Status.DRAFT);
		ProfileDetails savedProfile = profileRepo.save(profile);

		// Only link profile to OTP record if university email was provided (for candidates)
		// Interviewers don't need university email, so skip this step
		if (req.getUniversityEmail() != null && !req.getUniversityEmail().isBlank()) {
			otpService.linkProfile(req.getUniversityEmail(), savedProfile);
		}
		log.info("Personal details saved successfully for {}", user.getEmail());
		return toDto(savedProfile);
	}

	@Transactional
	public void verifyUniversityEmailOtp(String email, Integer otp) {
		log.info("Verifying OTP for university email: {}", email);
		if (otpService.verifyOtp(email, otp)) {
			Optional<ProfileDetails> p = profileRepo.findByEmail(email);
			p.ifPresent(profile -> {
				profile.setUniversityEmailVerified(true);
				profileRepo.save(profile);
				log.info("University email verified and updated for {}", email);
			});
		} else {
			log.warn("OTP verification failed for {}", email);
		}
	}

	@Transactional
	public ProfileDTO addEducation(Long userId, String email, List<EducationRequest> reqList) {
		log.info("Adding education details for email: {}", email);
		//Long userId = getCurrentUserId(email);
		ProfileDetails profile = profileRepo.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("Profile draft not found"));

		if (profile.getEducation() == null) {
			profile.setEducation(new ArrayList<>());
		}

		if (profile.getEducation() != null) {
			profile.getEducation().clear();
		} else {
			profile.setEducation(new ArrayList<>());
		}

		for (EducationRequest req : reqList) {
			Education e = Education.builder().profile(profile).educationLevel(req.getEducationLevel())
					.collegeName(req.getCollegeName()).areaOfStudy(req.getAreaOfStudy()).startDate(req.getStartDate())
					.endDate(req.getEndDate()).build();

			profile.getEducation().add(e);
		}

		profileRepo.save(profile);
		log.info("Education details saved successfully for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO addExperience(Long userId, String email, String role, ExperienceRequest req) {
		log.info("Adding experience details for {}", email);
		//Long userId = getCurrentUserId(email);

		ProfileDetails profile = profileRepo.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("Profile draft not found"));

		if (profile.getExperiences() != null) {
			profile.getExperiences().clear();
		} else {
			profile.setExperiences(new ArrayList<>());
		}

		Experience experience = new Experience();

		if (role.equalsIgnoreCase("CANDIDATE")) {
			if (req.getIsExperienced() == null) {
				throw new ValidationException("isExperienced field is required for candidates");
			}
			experience.setIsExperienced(req.getIsExperienced());
		} else if (role.equalsIgnoreCase("INTERVIEWER")) {
			experience.setIsExperienced(true);
		}
		experience.setProfile(profile);

		if (Boolean.TRUE.equals(req.getIsExperienced()) && req.getJobDetails() != null) {
			List<JobDetail> jobs = req.getJobDetails().stream()
					.map(j -> JobDetail.builder().companyName(j.getCompanyName()).jobTitle(j.getJobTitle())
							.location(j.getLocation())
							.startDate(j.getStartDate() != null ? j.getStartDate().toString() : null)
							.endDate(j.getEndDate() != null ? j.getEndDate().toString() : null)
							.currentlyWorking(j.getCurrentlyWorking()).roleDescription(j.getRoleDescription())
							.experience(experience).build())
					.toList();
			experience.setJobDetails(jobs);
		}

		profile.getExperiences().add(experience);
		profileRepo.save(profile);
		log.info("Experience details saved successfully for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO saveApplicationQuestions(Long userId, String email, ApplicationQuestionRequest req) {
		log.info("Saving application questions for {}", email);
		//Long userId = getCurrentUserId(email);
		ProfileDetails profile = profileRepo.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));

		if (profile.getStatus() == ProfileDetails.Status.SUBMITTED)
			throw new ConflictException("Profile already submitted");

		ApplicationQuestions question = appQuestionRepo.findById(profile.getId())
				.orElse(ApplicationQuestions.builder().profile(profile).build());

		question.setLegalAgeToWork(req.getLegalAgeToWork());
		question.setJobPreference(req.getJobPreference());
		question.setWillingToRelocate(req.getWillingToRelocate());
		question.setRequiresSponsorship(req.getRequireSponsorship());

		appQuestionRepo.save(question);
		profile.setApplicationQuestion(question);
		log.info("Application questions saved for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO saveVoluntaryDisclosures(Long userId, String email, VoluntaryDisclosureRequest req) {

		log.info("Saving voluntary disclosures for {}", email);
		//Long userId = getCurrentUserId(email);
		ProfileDetails profile = profileRepo.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));

		if (profile.getStatus() == ProfileDetails.Status.SUBMITTED)
			throw new ConflictException("Profile already submitted");

		VoluntaryDisclosure disclosure = voluntaryDisclosureRepo.findById(profile.getId())
				.orElse(VoluntaryDisclosure.builder().profile(profile).build());

		disclosure.setGender(req.getGender());
		disclosure.setRace(req.getRace());

		voluntaryDisclosureRepo.save(disclosure);
		profile.setVoluntaryDisclosure(disclosure);
		log.info("Voluntary disclosure saved successfully for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO saveProfileSkills(Long userId, String email, SkillSelectionRequest req) {
		log.info("Saving profile skills for {}", email);
		//Long userId = getCurrentUserId(email);

		ProfileDetails profile = profileRepo.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));

		if (profile.getStatus() == ProfileDetails.Status.SUBMITTED)
			throw new ConflictException("Profile already submitted");

		SkillGroup group = groupRepo.findById(req.getGroupId())
				.orElseThrow(() -> new NotFoundException("Invalid group ID"));

		SkillRole role = roleRepo.findById(req.getRoleId()).orElseThrow(() -> new NotFoundException("Invalid role ID"));

		List<CoreSkills> coreSkills = coreRepo.findAllById(req.getCoreSkillIds());

		if (coreSkills.size() != 10) {
			throw new IllegalArgumentException("You must select exactly 10 core skills.");
		}

		List<AdditionalSkill> additionalSkills = addRepo.findAllById(req.getAdditionalSkillIds());
		if (additionalSkills.size() != 5) {
			throw new IllegalArgumentException("You must select exactly 5 additional skills.");
		}

		ProfileSkills profileSkills = profileSkillRepo.findById(profile.getId())
				.orElse(ProfileSkills.builder().profile(profile).build());

		ProgrammingSkill program = programmingRepo.findById(req.getProgrammingId())
				.orElseThrow(() -> new NotFoundException("Invalid Programming ID"));

		profileSkills.setGroup(group);
		profileSkills.setRole(role);
		profileSkills.setCoreSkills(coreSkills);
		profileSkills.setAdditionalSkills(additionalSkills);
		profileSkills.setProgrammingSkill(program);
		profileSkillRepo.save(profileSkills);

		profile.setProfileSkills(profileSkills);
		log.info("Profile skills saved successfully for {}", email);
		return toDto(profile);
	}

	@Transactional
	public ProfileDTO submitProfile(Long userId, String email, String role) {
		log.info("Submitting profile for {}", email);

		try {
			//Long userId = getCurrentUserId(email);

			ProfileDetails profile = profileRepo.findByUserId(userId)
					.orElseThrow(() -> new NotFoundException("Profile not found for email: " + email));

			if (profile.getStatus() == ProfileDetails.Status.SUBMITTED) {
				throw new ConflictException("Profile already submitted for email: " + email);
			}

			if (profile.getFirstName() == null || profile.getLastName() == null) {
				throw new BadRequestException("Personal information incomplete for email: " + email);
			}

			// Only require university email verification for candidates, not for interviewers
			if (role.contains("CANDIDATE") && (profile.getUniversityEmail() == null || !profile.isUniversityEmailVerified())) {
				throw new BadRequestException("University email not verified for email: " + email);
			}

			if (role.contains("INTERVIEWER")) {
				profile.setProfileSkills(null);
				profile.setAssessmentStatus(AssessmentStatus.SUCCESS);
			}

			else if (role.contains("CANDIDATE")) {
				profile.setAssessmentStatus(AssessmentStatus.MOODLE_PENDING);
				if (profile.getProfileSkills() == null || profile.getProfileSkills().getCoreSkills().isEmpty()) {
					throw new IllegalArgumentException("Skills are required for candidates");
				}
			}

			String profileId = generateProfileId(role);
			profile.setProfileId(profileId);

			profile.setStatus(ProfileDetails.Status.SUBMITTED);

			profileRepo.save(profile);

			log.info("Profile submitted successfully for {}", email);

			try {
				if (role.contains("INTERVIEWER")) {
					emailService.registrationInterviewerSuccessfullEmail(email);
				} else if (role.contains("CANDIDATE")) {
					emailService.registrationCandidateSuccessfullEmail(email);
				}
			} catch (Exception mailEx) {
				log.error("Failed to send registration completion email to {}. Error: {}", email, mailEx.getMessage());
			}

			return toDto(profile);

		} catch (NotFoundException | BadRequestException | ConflictException ex) {
			log.warn("Profile submission failed for {}: {}", email, ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			log.error("Unexpected error while submitting profile for {}: {}", email, ex.getMessage(), ex);
			throw new RuntimeException("Failed to submit profile. Please try again later.");
		}
	}

	private String generateProfileId(String role) {

		String prefix;
		switch (role.toLowerCase()) {
		case "candidate" -> prefix = "AHC";
		case "interviewer" -> prefix = "AHI";
		case "employer" -> prefix = "AHE";
		case "admin" -> prefix = "AHA";
		default -> throw new IllegalArgumentException("Invalid role: " + role);
		}

		String profileId;

		do {
			profileId = prefix + (100000 + new Random().nextInt(900000));
		} while (profileRepo.findByProfileId(profileId).isPresent());

		log.debug("Generated unique profile ID: {}", profileId);

		return profileId;
	}

	private ProfileDTO toDto(ProfileDetails p) {
		log.debug("Converting ProfileDetails entity to DTO for userId: {}", p.getUserId());
		ProfileDTO dto = new ProfileDTO();
		dto.setId(p.getId());
		dto.setProfileId(p.getProfileId());
		dto.setUserId(p.getUserId());
		dto.setEmail(p.getEmail());
		dto.setFirstName(p.getFirstName());
		dto.setLastName(p.getLastName());
		dto.setUniversityEmail(p.getUniversityEmail());
		dto.setUniversityEmailVerified(p.isUniversityEmailVerified());
		dto.setPhoneNumber(p.getPhoneNumber());
		if (p.getAddress() != null) {
			dto.setAddress(p.getAddress().getAddress());
			dto.setCity(p.getAddress().getCity());
			dto.setState(p.getAddress().getState());
			dto.setCountry(p.getAddress().getCountry());
			dto.setPincode(p.getAddress().getPincode());
		}
		if (p.getPortfolio() != null) {
			dto.setLinkedIn(p.getPortfolio().getLinkedIn());
			dto.setGitHub(p.getPortfolio().getGitHub());
			dto.setPorfolioId(p.getPortfolio().getPorfolioId());
			dto.setOthers(p.getPortfolio().getOthers());
		}
		dto.setStatus(p.getStatus().name());
		if (p.getAssessmentStatus() != null) {
			dto.setAssessmentStatus(p.getAssessmentStatus().name());
		}
		dto.setCreatedAt(p.getCreatedAt());
		dto.setUpdatedAt(p.getUpdatedAt());

		if (p.getEducation() != null)
			dto.setEducations(p.getEducation().stream().map(e -> {
				EducationRequest er = new EducationRequest();
				er.setEducationLevel(e.getEducationLevel());
				er.setCollegeName(e.getCollegeName());
				er.setAreaOfStudy(e.getAreaOfStudy());
				er.setStartDate(e.getStartDate());
				er.setEndDate(e.getEndDate());
				return er;
			}).collect(Collectors.toList()));

		if (p.getExperiences() != null && !p.getExperiences().isEmpty()) {
			List<ExperienceResponse> experienceResponses = p.getExperiences().stream().map(exp -> {
				ExperienceResponse expRes = new ExperienceResponse();
				expRes.setIsExperienced(exp.getIsExperienced());
				if (Boolean.TRUE.equals(exp.getIsExperienced()) && exp.getJobDetails() != null) {
					List<JobDetailResponse> jobResponses = exp.getJobDetails().stream().map(j -> {
						JobDetailResponse jr = new JobDetailResponse();
						jr.setCompanyName(j.getCompanyName());
						jr.setJobTitle(j.getJobTitle());
						jr.setLocation(j.getLocation());
						jr.setStartDate(j.getStartDate() != null ? YearMonth.parse(j.getStartDate()) : null);
						jr.setEndDate(j.getEndDate() != null ? YearMonth.parse(j.getEndDate()) : null);
						jr.setCurrentlyWorking(j.getCurrentlyWorking());
						jr.setRoleDescription(j.getRoleDescription());
						return jr;
					}).toList();
					expRes.setJobDetails(jobResponses);
				}
				return expRes;
			}).toList();
			dto.setExperiences(experienceResponses);
		}

		if (p.getProfileSkills() != null) {
			ProfileSkills ps = p.getProfileSkills();

			ProfileSkillResponse skillRes = new ProfileSkillResponse();
			skillRes.setGroupName(ps.getGroup().getGroupName());
			skillRes.setRoleName(ps.getRole().getRoleName());
			skillRes.setCoreSkillNames(ps.getCoreSkills().stream().map(CoreSkills::getSkillName).toList());
			skillRes.setAdditionalSkillNames(
					ps.getAdditionalSkills().stream().map(AdditionalSkill::getSkillName).toList());
			skillRes.setProgrammingName(ps.getProgrammingSkill().getProgramName());
			dto.setSkills(skillRes);
		}
		if (p.getApplicationQuestion() != null) {
			ApplicationQuestionRequest aq = new ApplicationQuestionRequest();
			aq.setLegalAgeToWork(p.getApplicationQuestion().getLegalAgeToWork());
			aq.setWillingToRelocate(p.getApplicationQuestion().getWillingToRelocate());
			aq.setRequireSponsorship(p.getApplicationQuestion().getRequiresSponsorship());
			aq.setJobPreference(p.getApplicationQuestion().getJobPreference());

			dto.setApplicationReq(aq);
		}

		if (p.getVoluntaryDisclosure() != null) {
			VoluntaryDisclosureRequest vr = new VoluntaryDisclosureRequest();
			vr.setGender(p.getVoluntaryDisclosure().getGender());
			vr.setRace(p.getVoluntaryDisclosure().getRace());

			dto.setVoluntaryReq(vr);
		}

		return dto;
	}

	private ProfileDetails getProfile(String email) {
		log.info("Fetching profile for email={}", email);

		ProfileDetails profile = profileRepo.findByEmail(email).orElseThrow(() -> {
			log.warn("Profile not found for email={}", email);
			return new NotFoundException("Profile not found for: " + email);
		});

		log.debug("Profile fetched successfully for email={}, profileId={}", email, profile.getId());
		return profile;
	}

	public PersonalInfoResponse getPersonalInfo(String email) {
		log.info("Fetching personal information for {}", email);

		ProfileDetails p = getProfile(email);

		PersonalInfoResponse dto = new PersonalInfoResponse();

		dto.setFirstName(p.getFirstName());
		dto.setLastName(p.getLastName());
		dto.setUniversityEmail(p.getUniversityEmail());
		dto.setPhoneNumber(p.getPhoneNumber());

		if (p.getAddress() != null) {
			dto.setAddress(p.getAddress().getAddress());
			dto.setCity(p.getAddress().getCity());
			dto.setState(p.getAddress().getState());
			dto.setCountry(p.getAddress().getCountry());
			dto.setPincode(p.getAddress().getPincode());
			log.debug("Address details set for {}", email);
		} else {
			log.debug("No address details found for {}", email);
		}

		if (p.getPortfolio() != null) {
			dto.setLinkedIn(p.getPortfolio().getLinkedIn());
			dto.setGitHub(p.getPortfolio().getGitHub());
			dto.setPorfolioId(p.getPortfolio().getPorfolioId());
			dto.setOthers(p.getPortfolio().getOthers());
			log.debug("Portfolio details set for {}", email);
		} else {
			log.debug("No portfolio details found for {}", email);
		}

		log.info("Personal information fetched successfully for {}", email);
		return dto;
	}

	public List<EducationResponse> getEducation(String email) {
		log.info("Fetching education details for {}", email);

		Long profileId = getProfile(email).getId();
		log.debug("Fetching education using profileId={} for {}", profileId, email);

		List<EducationResponse> response = educationRepo.findByProfile_Id(profileId).stream().map(e -> {
			EducationResponse er = new EducationResponse();
			er.setEducationLevel(e.getEducationLevel());
			er.setCollegeName(e.getCollegeName());
			er.setAreaOfStudy(e.getAreaOfStudy());
			er.setStartDate(e.getStartDate());
			er.setEndDate(e.getEndDate());
			return er;
		}).toList();

		log.info("Education details fetched successfully for {}, count={}", email, response.size());
		return response;
	}

	public List<ExperienceResponse> getExperience(String email) {
		log.info("Fetching experience details for {}", email);

		Long profileId = getProfile(email).getId();
		log.debug("Fetching experience using profileId={} for {}", profileId, email);

		List<ExperienceResponse> response = experienceRepo.findByProfile_Id(profileId).stream().map(exp -> {
			ExperienceResponse expRes = new ExperienceResponse();
			expRes.setIsExperienced(exp.getIsExperienced());

			if (Boolean.TRUE.equals(exp.getIsExperienced()) && exp.getJobDetails() != null) {

				List<JobDetailResponse> jobs = exp.getJobDetails().stream().map(j -> {
					JobDetailResponse jr = new JobDetailResponse();
					jr.setCompanyName(j.getCompanyName());
					jr.setJobTitle(j.getJobTitle());
					jr.setLocation(j.getLocation());
					jr.setStartDate(j.getStartDate() != null ? YearMonth.parse(j.getStartDate()) : null);
					jr.setEndDate(j.getEndDate() != null ? YearMonth.parse(j.getEndDate()) : null);
					jr.setCurrentlyWorking(j.getCurrentlyWorking());
					jr.setRoleDescription(j.getRoleDescription());
					return jr;
				}).toList();

				expRes.setJobDetails(jobs);
			}

			return expRes;
		}).toList();

		log.info("Experience details fetched successfully for {}, count={}", email, response.size());
		return response;
	}

	public ProfileSkillResponse getProfileSkills(String email) {
		log.info("Fetching profile skills for {}", email);

		Long profileId = getProfile(email).getId();
		log.debug("Fetching profile skills using profileId={} for {}", profileId, email);

		ProfileSkills ps = profileSkillRepo.findByProfile_Id(profileId).orElseThrow(() -> {
			log.warn("Skills not found for email={}, profileId={}", email, profileId);
			return new NotFoundException("Skills not found");
		});

		ProfileSkillResponse res = new ProfileSkillResponse();
		res.setGroupName(ps.getGroup().getGroupName());
		res.setRoleName(ps.getRole().getRoleName());
		res.setProgrammingName(ps.getProgrammingSkill().getProgramName());

		res.setCoreSkillNames(ps.getCoreSkills().stream().map(CoreSkills::getSkillName).toList());
		res.setAdditionalSkillNames(ps.getAdditionalSkills().stream().map(AdditionalSkill::getSkillName).toList());

		log.info("Profile skills fetched successfully for {}", email);
		return res;
	}

	public ApplicationQuestionResponse getApplicationQuestions(String email) {
		log.info("Fetching application questions for {}", email);

		Long profileId = getProfile(email).getId();
		log.debug("Fetching application questions using profileId={} for {}", profileId, email);

		ApplicationQuestions aq = appQuestionRepo.findByProfile_Id(profileId).orElseThrow(() -> {
			log.warn("Questions not found for email={}, profileId={}", email, profileId);
			return new NotFoundException("Questions not found");
		});

		ApplicationQuestionResponse res = new ApplicationQuestionResponse();
		res.setLegalAgeToWork(aq.getLegalAgeToWork());
		res.setJobPreference(aq.getJobPreference());
		res.setWillingToRelocate(aq.getWillingToRelocate());
		res.setRequireSponsorship(aq.getRequiresSponsorship());

		log.info("Application questions fetched successfully for {}", email);
		return res;
	}

	public VoluntaryDisclosureRequest getVoluntaryDisclosure(String email) {
		log.info("Fetching voluntary disclosure for {}", email);

		Long profileId = getProfile(email).getId();
		log.debug("Fetching voluntary disclosure using profileId={} for {}", profileId, email);

		VoluntaryDisclosure vd = voluntaryDisclosureRepo.findByProfile_Id(profileId).orElseThrow(() -> {
			log.warn("Voluntary data not found for email={}, profileId={}", email, profileId);
			return new NotFoundException("Voluntary data not found");
		});

		VoluntaryDisclosureRequest res = new VoluntaryDisclosureRequest();
		res.setGender(vd.getGender());
		res.setRace(vd.getRace());

		log.info("Voluntary disclosure fetched successfully for {}", email);
		return res;
	}

	public List<CandidateDetailsForInterview> getQuizPassedCandidates() {

	    log.info("Fetching quiz passed candidates for interview");

	    List<ProfileRepository.CandidateDetailsForInterview> projections =
	            profileRepo.findByAssessmentStatusForInterview(ProfileDetails.AssessmentStatus.QUIZ_PASSED);

	    if (projections == null || projections.isEmpty()) {
	        log.info("No quiz passed candidates found");
	        return Collections.emptyList();   // ✅ returns []
	    }

	    List<CandidateDetailsForInterview> response = projections.stream()
	            .map(p -> new CandidateDetailsForInterview(
	                    p.getProfileId(),
	                    p.getEmail(),
	                    p.getFirstName(),
	                    p.getLastName(),
	                    p.getDomainName()
	            ))
	            .collect(Collectors.toList());

	    log.info("Quiz passed candidates fetched successfully, count={}", response.size());
	    return response;
	}


	@Transactional
	public void updateAssessmentStatus(String profileId, String status) {
		log.info("Updating assessment status. profileId={}, status={}", profileId, status);

		if (profileId == null || profileId.isBlank()) {
			log.warn("Invalid profileId provided for assessment status update: {}", profileId);
			throw new BadRequestException("profileId must not be empty");
		}

		if (status == null || status.isBlank()) {
			log.warn("Invalid status provided for assessment status update. profileId={}, status={}", profileId,
					status);
			throw new BadRequestException("status must not be empty");
		}

		ProfileDetails profile = profileRepo.findByProfileId(profileId).orElseThrow(() -> {
			log.warn("Profile not found with profileId={}", profileId);
			return new UserNotFoundException("Profile not found with profileId: " + profileId);
		});

		ProfileDetails.AssessmentStatus assessmentStatus;
		try {
			assessmentStatus = ProfileDetails.AssessmentStatus.valueOf(status.toUpperCase());
		} catch (IllegalArgumentException ex) {
			log.warn("Invalid assessment status provided: {} for profileId={}", status, profileId);
			throw new BadRequestException("Invalid assessment status: " + status);
		}

		profile.setAssessmentStatus(assessmentStatus);
		profileRepo.save(profile);

		log.info("Assessment status updated successfully. profileId={}, newStatus={}", profileId, assessmentStatus);
	}

	@Transactional
	public CandidateProfileLeftViewResponse getCandidateLeftPanel(String profileId) {
		log.info("Fetching candidate left panel details for profileId={}", profileId);

		try {
			ProfileDetails profile = profileRepo.fetchLeftPanelData(profileId).orElseThrow(() -> {
				log.warn("Profile not found while fetching left panel for profileId={}", profileId);
				return new ProfileNotFoundException("Profile not found: " + profileId);
			});

			String fullName = (profile.getFirstName() == null ? "" : profile.getFirstName()) + " "
					+ (profile.getLastName() == null ? "" : profile.getLastName());
			fullName = fullName.trim();

			String role = "N/A";
			if (profile.getProfileSkills() != null && profile.getProfileSkills().getRole() != null) {
				role = profile.getProfileSkills().getRole().toString();
			}

			List<String> skills = new ArrayList<>();
			if (profile.getProfileSkills() != null && profile.getProfileSkills().getCoreSkills() != null) {
				skills = profile.getProfileSkills().getCoreSkills().stream().map(CoreSkills::getSkillName)
						.filter(Objects::nonNull).distinct().collect(Collectors.toList());
			}

			String positionType = "N/A";
			if (profile.getApplicationQuestion() != null
					&& profile.getApplicationQuestion().getJobPreference() != null) {
				positionType = profile.getApplicationQuestion().getJobPreference().name();
			}

			String location = "N/A";
			if (profile.getAddress() != null) {
				String city = profile.getAddress().getCity();
				String state = profile.getAddress().getState();

				if (city != null && state != null) {
					location = city + ", " + state;
				} else if (city != null) {
					location = city;
				} else if (state != null) {
					location = state;
				}
			}

			Boolean isExperienced = false;
			List<ExperienceLeftViewResponse> experienceResponses = new ArrayList<>();

			if (profile.getExperiences() != null && !profile.getExperiences().isEmpty()) {
				Experience exp = profile.getExperiences().get(0);
				isExperienced = exp.getIsExperienced() != null && exp.getIsExperienced();

				if (exp.getJobDetails() != null) {
					experienceResponses = exp.getJobDetails().stream()
							.map(j -> ExperienceLeftViewResponse.builder().companyName(j.getCompanyName())
									.jobTitle(j.getJobTitle()).startDate(j.getStartDate()).endDate(j.getEndDate())
									.build())
							.collect(Collectors.toList());
				}
			}

			CandidateProfileLeftViewResponse response = CandidateProfileLeftViewResponse.builder()
					.profileId(profile.getProfileId()).fullName(fullName).role(role).skills(skills)
					.positionType(positionType).location(location).isExperienced(isExperienced)
					.experience(experienceResponses).build();

			log.info("Candidate left panel details fetched successfully for profileId={}", profileId);
			return response;

		} catch (ProfileNotFoundException ex) {
			log.warn("ProfileNotFoundException while fetching left panel for profileId={}", profileId);
			throw ex;
		} catch (Exception ex) {
			log.error("Unexpected error while fetching profile left panel details for profileId={}", profileId, ex);
			throw new RuntimeException("Failed to fetch profile left panel details", ex);
		}
	}

	@Transactional
	public List<CandidateProfileDTO> getCandidatesByRoleAndStatus(String jobRole, String status, int limit) {

		try {
			if (jobRole == null || jobRole.trim().isEmpty()) {
				throw new IllegalArgumentException("jobRole is required");
			}

			if (status == null || status.trim().isEmpty()) {
				throw new IllegalArgumentException("status is required");
			}

			if (limit <= 0)
				limit = 10;
			if (limit > 50)
				limit = 50;

			ProfileDetails.AssessmentStatus assessmentStatus;

			try {
			    assessmentStatus = ProfileDetails.AssessmentStatus.valueOf(status.toUpperCase());
			} catch (Exception ex) {
			    assessmentStatus = ProfileDetails.AssessmentStatus.COMPLETED_T1;
			}

			List<ProfileDetails> profiles =
			        profileRepo.findCandidatesByJobRoleAndAssessmentStatus(jobRole, assessmentStatus);


			if (profiles == null || profiles.isEmpty()) {
				return Collections.emptyList();
			}

			return profiles.stream().map(this::mapToCandidateDTO).limit(limit).collect(Collectors.toList());

		} catch (Exception ex) {
			throw new RuntimeException("Error while fetching candidates by role/status", ex);
		}
	}

	private CandidateProfileDTO mapToCandidateDTO(ProfileDetails p) {

		String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " "
				+ (p.getLastName() != null ? p.getLastName() : "")).trim();

		ProfileSkills ps = p.getProfileSkills();

		String roleName = null;
		if (ps != null && ps.getRole() != null) {
			roleName = ps.getRole().getRoleName(); // ✅ correct
		}

		Set<String> coreSkills = new HashSet<>();
		if (ps != null && ps.getCoreSkills() != null) {
			coreSkills = ps.getCoreSkills().stream().map(CoreSkills::getSkillName).filter(Objects::nonNull)
					.collect(Collectors.toSet());
		}

		Integer totalExperience = 0;

		return CandidateProfileDTO.builder().profileId(p.getProfileId()).name(fullName).email(p.getEmail())
				.role(roleName).totalExperience(totalExperience).coreSkills(coreSkills)
				.status(p.getAssessmentStatus().name()).build();
	}

}
