package com.alphonso.profile_service.ResponseDTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.alphonso.profile_service.RequestDTO.ApplicationQuestionRequest;
import com.alphonso.profile_service.RequestDTO.EducationRequest;
import com.alphonso.profile_service.RequestDTO.VoluntaryDisclosureRequest;

@Data
public class ProfileDTO {
	private Long id;
	private String profileId;
	private Long userId;
	private String email;
	private String firstName;
	private String lastName;
	private String universityEmail;
	private boolean universityEmailVerified;
	private String phoneNumber;
	private String address;
	private String city;
	private String state;
	private String country;
	private Integer pincode;
	private String status;
	private String assessmentStatus;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String linkedIn;

	private String gitHub;

	private String porfolioId;

	private String others;
	private List<EducationRequest> educations;
	private List<ExperienceResponse> experiences;
	private ProfileSkillResponse skills;
	private ApplicationQuestionRequest applicationReq;
	private VoluntaryDisclosureRequest voluntaryReq;
	

}
