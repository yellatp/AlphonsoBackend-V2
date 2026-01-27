package com.alphonso.profile_service.Service;

import java.util.List;
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
import com.alphonso.profile_service.ResponseDTO.ExperienceResponse;
import com.alphonso.profile_service.ResponseDTO.InterviewerDTO;
import com.alphonso.profile_service.ResponseDTO.PersonalInfoResponse;
import com.alphonso.profile_service.ResponseDTO.ProfileDTO;
import com.alphonso.profile_service.ResponseDTO.ProfileSkillResponse;
import com.alphonso.profile_service.ResponseDTO.UserDTO;

public interface IProfileService {

	public ProfileDTO getOrCreateDraft(Long userId, String email);

	public ProfileDTO getOrCreateProfile(UserDTO user);

	public ProfileDTO savePersonal(UserDTO user, PersonalInfoRequest req);

	public void verifyUniversityEmailOtp(String email, Integer otp);

	public ProfileDTO addEducation(Long userId, String email, List<EducationRequest> reqList);

	public ProfileDTO addExperience(Long userId, String email, String role, ExperienceRequest req);

	public ProfileDTO saveApplicationQuestions(Long userId, String email, ApplicationQuestionRequest req);

	public ProfileDTO saveVoluntaryDisclosures(Long userId, String email, VoluntaryDisclosureRequest req);

	public ProfileDTO saveProfileSkills(Long userId, String email, SkillSelectionRequest req);

	public ProfileDTO submitProfile(Long userId, String email, String role);
	
	public ProfileDTO getProfileDetails(String email, String role);
	
	public InterviewerDTO getProfileByEmail(String email);
	
	public PersonalInfoResponse getPersonalInfo(String email);
	
	public List<EducationResponse> getEducation(String email);
	
	public List<ExperienceResponse> getExperience(String email);
	
	public ProfileSkillResponse getProfileSkills(String email);
	
	public ApplicationQuestionResponse getApplicationQuestions(String email);
	
	public VoluntaryDisclosureRequest getVoluntaryDisclosure(String email);
	
	public List<CandidateDetailsForInterview> getQuizPassedCandidates();
	
	public void updateAssessmentStatus(String profileId, String status);
	
	public CandidateProfileLeftViewResponse getCandidateLeftPanel(String profileId);
	
	public List<CandidateProfileDTO> getCandidatesByRoleAndStatus(String jobRole, String status, int limit);
}
