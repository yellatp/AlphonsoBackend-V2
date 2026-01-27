package com.alphonso.moodle_employer_service.Moodle.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleCourseMappingEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile;
import com.alphonso.moodle_employer_service.Moodle.Entity.ProfileSkillEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillEntity;
import com.alphonso.moodle_employer_service.Moodle.Repository.MoodleCourseMappingRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.MoodleProfileRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.ProfileSkillRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.SkillRepository;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.MoodleRequest;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.SyncResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoodleUserSyncService {

	private final MoodleApiService moodleService;
	private final MoodleProfileRepository profileRepo;
	private final SkillRepository skillRepo;
	private final ProfileSkillRepository profileSkillRepo;
	private final MoodleCourseMappingRepository moodleCourseMappingRepo;
	private final MoodleAssessmentService moodleAssessment;

	public SyncResponse createUserAndLinkSkills(MoodleRequest request) {
		
		SyncResponse resp=null;
		
		try {
			Long moodleUserId = moodleService.createOrGetUser(request.getFirstName(), request.getLastName(),
					request.getEmail(), false);
			if (moodleUserId == null) {
				return new SyncResponse(false, null, "Could not obtain Moodle user id");
			}

			MoodleProfile profile = profileRepo.findByProfileId(request.getProfileId()).orElseGet(MoodleProfile::new);
			profile.setProfileId(request.getProfileId());
			profile.setUsername(request.getEmail());
			profile.setEmail(request.getEmail());
			profile.setMoodleUserId(moodleUserId);
			profileRepo.save(profile);
			
			List<String> allSkills = new ArrayList<>(request.getSkills());
			if (!allSkills.contains("Assessment Host Course")) {
				allSkills.add("Assessment Host Course");
			}

			for (String rawSkillName : allSkills) {
				String skillName = rawSkillName.trim();

				SkillEntity skill = skillRepo.findBySkillNameIgnoreCase(skillName).orElseGet(() -> {
					SkillEntity s = new SkillEntity();
					s.setSkillName(skillName);
					return skillRepo.save(s);
				});

				if (skill.getCohortId() == null) {
					MoodleCourseMappingEntity mapping = moodleCourseMappingRepo.findBySkillName(skillName).orElse(null);
					if (mapping != null && mapping.getCohortId() != null) {
						skill.setCohortId(mapping.getCohortId());
						skillRepo.save(skill);
					}
				}

				if (skill.getCohortId() != null) {
					boolean added = moodleService.addUserToCohort(moodleUserId, skill.getCohortId());
					if (!added) {
						log.warn("Could not add {} to cohort {}", request.getEmail(), skill.getCohortId());
					}
				}

				if (!profileSkillRepo.existsByUserAndSkill(profile, skill)) {
					ProfileSkillEntity ps = new ProfileSkillEntity();
					ps.setUser(profile);
					ps.setSkill(skill);
					profileSkillRepo.save(ps);
				}
			}
			
			
			if (moodleUserId != null && moodleUserId != 0L) {
			    resp = new SyncResponse(true, moodleUserId, "OK");
			} else {
			    resp = new SyncResponse(false, null, "Moodle Sync Unseccessfull");
			}

			moodleAssessment.bootstrapQcats("AssessmentHost");
		} catch (Exception e) {
			log.error("Failed to sync user {}: {}", request.getEmail(), e.getMessage(), e);
		}
		return resp;
	}

}