package com.alphonso.profile_service.ServiceImp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.alphonso.profile_service.Entity.CoreSkills;
import com.alphonso.profile_service.Entity.ProfileDetails;
import com.alphonso.profile_service.Entity.ProfileDetails.AssessmentStatus;
import com.alphonso.profile_service.Repository.ProfileRepository;
import com.alphonso.profile_service.RequestDTO.MoodleRequest;
import com.alphonso.profile_service.ResponseDTO.ProfileMoodleResultResponse;
import com.alphonso.profile_service.ResponseDTO.SyncResponse;
import com.alphonso.profile_service.Service.IEmailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
@Slf4j
public class ProfileSyncJob {

	private final ProfileRepository profileRepository;
	//private final MoodleServiceClient moodleClient;
	private final IEmailService emailService;
	private final MoodleCircuitBreakerService moodleCircuitBreaker;

	public ProfileSyncJob(ProfileRepository profileRepository,
			IEmailService emailService, MoodleCircuitBreakerService moodleCircuitBreaker) {
		this.profileRepository = profileRepository;
		//this.moodleClient = moodleClient;
		this.emailService = emailService;
		this.moodleCircuitBreaker = moodleCircuitBreaker;
	}

	// TODO: Change back to "0 0 * * * *" (every 1 hour) after testing
	@Scheduled(cron = "0 0/15 * * * *") // Currently: every 15 min for testing
	@SchedulerLock(name = "syncPendingProfiles", lockAtMostFor = "5m", lockAtLeastFor = "1m")
	@Transactional
	public void syncPendingProfiles() {
		log.info("=== Starting syncPendingProfiles job ===");
		
		List<ProfileDetails> pending = profileRepository.findByAssessmentStatus(AssessmentStatus.MOODLE_PENDING);
		log.info("Found {} profiles with MOODLE_PENDING status", pending.size());

		if (pending.isEmpty()) {
			log.info("No pending profiles to sync. Exiting syncPendingProfiles job.");
			return;
		}

		int successCount = 0;
		int failureCount = 0;

		for (ProfileDetails profile : pending) {
			String profileEmail = profile.getEmail();
			String profileId = profile.getProfileId();
			
			log.info("Processing profile - Email: {}, ProfileId: {}, Status: {}", 
					profileEmail, profileId, profile.getAssessmentStatus());
			
			try {
				// Check if profileSkills is null
				if (profile.getProfileSkills() == null) {
					log.error("ProfileSkills is NULL for profile - Email: {}, ProfileId: {}. Skipping sync.", 
							profileEmail, profileId);
					failureCount++;
					continue;
				}
				
				log.debug("ProfileSkills found for profile: {}", profileEmail);
				
				// Check if coreSkills is null or empty
				if (profile.getProfileSkills().getCoreSkills() == null) {
					log.error("CoreSkills is NULL for profile - Email: {}, ProfileId: {}. Skipping sync.", 
							profileEmail, profileId);
					failureCount++;
					continue;
				}
				
				List<String> coreSkillNames = profile.getProfileSkills().getCoreSkills().stream()
						.map(CoreSkills::getSkillName).collect(Collectors.toList());
				
				if (coreSkillNames.isEmpty()) {
					log.warn("CoreSkills list is EMPTY for profile - Email: {}, ProfileId: {}. Proceeding with empty skills list.", 
							profileEmail, profileId);
				} else {
					log.debug("Found {} core skills for profile: {}", coreSkillNames.size(), profileEmail);
				}

				profile.setAssessmentStatus(AssessmentStatus.MOODLE_PENDING);
				profile.setUpdatedAt(LocalDateTime.now());
				
				MoodleRequest dto = new MoodleRequest(profile.getProfileId(), profile.getFirstName(),
						profile.getLastName(), profile.getEmail(), coreSkillNames);
				
				log.debug("Calling moodleCircuitBreaker.syncUser for profile: {}", profileEmail);
				SyncResponse resp = moodleCircuitBreaker.syncUser(dto);

				if (resp == null) {
					log.error("Moodle sync user response is NULL for profile - Email: {}, ProfileId: {}", 
							profileEmail, profileId);
					throw new RuntimeException("Moodle sync user response is empty");
				}

				log.info("Moodle sync response received for profile: {} - Success: {}, Response: {}", 
						profileEmail, resp.success(), resp);
				
				if (resp != null && resp.success()) {
					profile.setAssessmentStatus(AssessmentStatus.MOODLE_SYNC_SUCCESS);
					profile.setUpdatedAt(LocalDateTime.now());
					log.info("Successfully synced profile to Moodle - Email: {}, ProfileId: {}", 
							profileEmail, profileId);
					successCount++;

				} else if (resp != null && !resp.success()) {
					profile.setAssessmentStatus(AssessmentStatus.MOODLE_SYNC_FAILED);
					log.warn("Moodle sync failed for profile - Email: {}, ProfileId: {}, Response: {}", 
							profileEmail, profileId, resp);
					failureCount++;

				} else {
					profile.setAssessmentStatus(AssessmentStatus.MOODLE_PENDING);
					log.warn("Moodle sync response is ambiguous for profile - Email: {}, ProfileId: {}", 
							profileEmail, profileId);
					failureCount++;
				}

				profileRepository.saveAndFlush(profile);
				log.debug("Profile saved to database - Email: {}", profileEmail);

				try {
					emailService.moodleQuizEnableEmail(profile.getEmail());
					log.debug("Quiz enable email sent successfully to: {}", profileEmail);
				} catch (Exception mailEx) {
					log.error("Failed to send quiz enable email to {}. Error: {}", profileEmail,
							mailEx.getMessage(), mailEx);
				}

			} catch (NullPointerException npe) {
				log.error("NullPointerException while processing profile - Email: {}, ProfileId: {}. " +
						"Error: {}. Stack trace: {}", 
						profileEmail, profileId, npe.getMessage(), 
						java.util.Arrays.toString(npe.getStackTrace()), npe);
				failureCount++;
				try {
					profileRepository.saveAndFlush(profile);
				} catch (Exception saveEx) {
					log.error("Failed to save profile after error - Email: {}. Error: {}", 
							profileEmail, saveEx.getMessage(), saveEx);
				}
			} catch (Exception e) {
				log.error("Unexpected error while submitting profile - Email: {}, ProfileId: {}. " +
						"Error: {}. Stack trace: {}", 
						profileEmail, profileId, e.getMessage(), 
						java.util.Arrays.toString(e.getStackTrace()), e);
				failureCount++;
				try {
					profileRepository.saveAndFlush(profile);
				} catch (Exception saveEx) {
					log.error("Failed to save profile after error - Email: {}. Error: {}", 
							profileEmail, saveEx.getMessage(), saveEx);
				}
			}
		}
		
		log.info("=== Completed syncPendingProfiles job - Success: {}, Failed: {}, Total: {} ===", 
				successCount, failureCount, pending.size());
	}

	// TODO: Change back to "0 0 * * * *" (every 1 hour) after testing
	@Scheduled(cron = "0 0/15 * * * *") // Currently: every 15 min for testing
	@SchedulerLock(name = "syncProfileResultsProfiles", lockAtMostFor = "5m", lockAtLeastFor = "1m")
	@Transactional
	public void syncProfileResultsProfiles() {
		log.info("=== Starting syncProfileResultsProfiles job ===");
		
		List<ProfileDetails> moodleProfileDetails = profileRepository
				.findByAssessmentStatus(AssessmentStatus.MOODLE_SYNC_SUCCESS);
		
		log.info("Found {} profiles with MOODLE_SYNC_SUCCESS status", moodleProfileDetails.size());

		List<String> moodleProfileIds = moodleProfileDetails.stream().map(ProfileDetails::getProfileId)
				.filter(Objects::nonNull).collect(Collectors.toList());
		
		log.info("Processing {} profile IDs for result sync", moodleProfileIds.size());

		if (moodleProfileIds.isEmpty()) {
			log.info("No profiles to sync results for. Exiting syncProfileResultsProfiles job.");
			return;
		}

		int successCount = 0;
		int failureCount = 0;
		int[] notFoundCount = new int[1]; // Use array to make it effectively final for lambda

		for (String moodleProfileId : moodleProfileIds) {
			log.debug("Processing result sync for profileId: {}", moodleProfileId);
			
			try {
				ProfileMoodleResultResponse results = moodleCircuitBreaker.syncAttempts(moodleProfileId);
				log.debug("Moodle syncAttempts response received for profileId: {} - Status: {}", 
						moodleProfileId, results != null ? results.getStatus() : "NULL");

				if (results == null) {
					log.error("Moodle sync result is NULL for profileId: {}", moodleProfileId);
					throw new RuntimeException("Moodle sync result is empty for profileId: " + moodleProfileId);
				}
				
				profileRepository.findByProfileId(moodleProfileId).ifPresentOrElse(pd -> {
					String profileEmail = pd.getEmail();
					log.debug("Found profile in database - Email: {}, ProfileId: {}", profileEmail, moodleProfileId);
					
					String status = results.getStatus();
					log.debug("Current status from Moodle: {} for profile: {}", status, profileEmail);

					if (status != null) {
						String normalized = status.trim().toUpperCase();
						log.debug("Normalized status: {} for profile: {}", normalized, profileEmail);

						try {
							if (normalized.equals("QUIZ_COMPLETED") || normalized.equals("INPROGRESS")) {
								pd.setAssessmentStatus(AssessmentStatus.MOODLE_SYNC_SUCCESS);
								log.info("Updated profile status to MOODLE_SYNC_SUCCESS - Email: {}, ProfileId: {}", 
										profileEmail, moodleProfileId);
							} else {
								AssessmentStatus newStatus = AssessmentStatus.valueOf(normalized);
								pd.setAssessmentStatus(newStatus);
								log.info("Updated profile status to {} - Email: {}, ProfileId: {}", 
										newStatus, profileEmail, moodleProfileId);
							}
						} catch (IllegalArgumentException iae) {
							log.error("Invalid AssessmentStatus value: {} for profile - Email: {}, ProfileId: {}. " +
									"Error: {}", normalized, profileEmail, moodleProfileId, iae.getMessage(), iae);
						}
					} else {
						log.warn("Status is NULL in Moodle response for profile - Email: {}, ProfileId: {}", 
								profileEmail, moodleProfileId);
					}

					try {
						profileRepository.save(pd);
						log.debug("Profile saved successfully - Email: {}", profileEmail);
					} catch (Exception saveEx) {
						log.error("Failed to save profile - Email: {}, ProfileId: {}. Error: {}", 
								profileEmail, moodleProfileId, saveEx.getMessage(), saveEx);
					}

					try {
						String normalized = status == null ? "" : status.trim().toUpperCase();
						log.debug("Checking email notification for status: {} - Email: {}", normalized, profileEmail);

						if (normalized.equals("QUIZ_PASSED")) {
							emailService.moodleQuizPassEmail(pd.getEmail());
							log.info("Quiz pass email sent to: {}", profileEmail);
						} else if (normalized.equals("QUIZ_FAILED")) {
							emailService.moodleQuizFailEmail(pd.getEmail());
							log.info("Quiz fail email sent to: {}", profileEmail);
						} else {
							log.debug("No email notification needed for status: {} - Email: {}", normalized, profileEmail);
						}
					} catch (Exception mailEx) {
						log.error("Failed to send email notification to {}. Error: {}", profileEmail,
								mailEx.getMessage(), mailEx);
					}
				}, () -> {
					log.warn("Profile not found in database for profileId: {}", moodleProfileId);
					notFoundCount[0]++;
				});
				
				successCount++;
				
			} catch (RuntimeException rte) {
				log.error("RuntimeException while syncing results for profileId: {}. Error: {}", 
						moodleProfileId, rte.getMessage(), rte);
				failureCount++;
			} catch (Exception e) {
				log.error("Unexpected error while syncing results for profileId: {}. Error: {}. Stack trace: {}", 
						moodleProfileId, e.getMessage(), 
						java.util.Arrays.toString(e.getStackTrace()), e);
				failureCount++;
			}
		}
		
		log.info("=== Completed syncProfileResultsProfiles job - Success: {}, Failed: {}, Not Found: {}, Total: {} ===", 
				successCount, failureCount, notFoundCount[0], moodleProfileIds.size());
	}
}