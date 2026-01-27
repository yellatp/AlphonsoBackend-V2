package com.alphonso.moodle_employer_service.Moodle.Schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Moodle.Config.MoodleConfig;
import com.alphonso.moodle_employer_service.Moodle.Entity.CourseEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleCourseMappingEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile.Status;
import com.alphonso.moodle_employer_service.Moodle.Exception.MoodleSyncException;
import com.alphonso.moodle_employer_service.Moodle.Repository.CourseRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.MoodleCourseMappingRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.MoodleProfileRepository;
import com.alphonso.moodle_employer_service.Moodle.Service.MoodleApiService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodleSyncService {

	private final MoodleApiService moodleService;
	private final CourseRepository courseRepo;
	private final MoodleCourseMappingRepository mappingRepo;
	private final MoodleConfig moodleConfig;
	private final MoodleProfileRepository moodleProfileRepo;

	// TODO: Change back to "0 0 * * * *" (every 1 hour) after testing
	@Scheduled(cron = "0 0/15 * * * *") // Currently: every 15 min for testing
	public void syncCoursesWithCategoryCohort() {
		log.info("=== Starting syncCoursesWithCategoryCohort job ===");

		int successCount = 0;
		int skippedCount = 0;
		int failureCount = 0;

		try {
			log.debug("Fetching courses from Moodle subcategories (categoryId: 2)");
			List<Map<String, Object>> courses = moodleService.getAllCoursesFromSubcategories(2L);

			if (courses == null || courses.isEmpty()) {
				log.warn("No courses retrieved from Moodle! Courses list is null or empty.");
				log.info("=== Completed syncCoursesWithCategoryCohort job - No courses found ===");
				return;
			}

			log.info("Retrieved {} courses from Moodle", courses.size());

			for (Map<String, Object> course : courses) {
				Long moodleCourseId = null;
				String courseFullName = null;
				
				try {
					moodleCourseId = ((Number) course.get("id")).longValue();
					courseFullName = (String) course.get("fullname");
					
					// Make final for lambda usage
					final Long finalCourseId = moodleCourseId;
					final String finalCourseName = courseFullName;
					
					log.debug("Processing course - ID: {}, Name: {}", moodleCourseId, courseFullName);

					if (moodleCourseId == 1L) {
						log.debug("Skipping course with ID 1 (default Moodle course)");
						skippedCount++;
						continue;
					}

					String shortName = (String) course.get("shortname");
					String summary = (String) course.get("summary");
					Long categoryId = ((Number) course.get("categoryid")).longValue();
					Boolean visible = ((Number) course.get("visible")).intValue() == 1;

					log.debug("Course details - ShortName: {}, CategoryId: {}, Visible: {}", 
							shortName, categoryId, visible);

					// Check if course exists in database, create if not
					final String finalShortName = shortName;
					final String finalSummary = summary;
					final Long finalCategoryId = categoryId;
					final Boolean finalVisible = visible;
					
					courseRepo.findByMoodleCourseId(moodleCourseId).orElseGet(() -> {
						log.info("Course not found in database, creating new course entity - ID: {}, Name: {}", 
								finalCourseId, finalCourseName);
						CourseEntity c = new CourseEntity();
						c.setMoodleCourseId(finalCourseId);
						c.setFullName(finalCourseName);
						c.setShortName(finalShortName);
						c.setSummary(finalSummary);
						c.setCategoryId(finalCategoryId);
						c.setVisible(finalVisible != null ? finalVisible : true);
						c.setCreatedAt(LocalDateTime.now());
						c.setLastSynced(LocalDateTime.now());
						CourseEntity saved = courseRepo.save(c);
						log.info("Created new course entity - ID: {}, Name: {}", finalCourseId, finalCourseName);
						return saved;
					});

					// Check if mapping already exists
					if (mappingRepo.existsByMoodleCourseId(moodleCourseId)) {
						log.info("Course '{}' (ID: {}) already mapped with cohort. Skipping.", 
								courseFullName, moodleCourseId);
						skippedCount++;
						continue;
					}

					String cohortName = "Cohort_" + courseFullName.replace(" ", "_");
					String cohortIdNumber = "CH_" + moodleCourseId;

					log.info("Creating cohort for course - Course: {}, CohortName: {}, CohortIdNumber: {}", 
							courseFullName, cohortName, cohortIdNumber);

					List<Map<String, Object>> cohortResp = null;
					try {
						cohortResp = moodleService.createCohort(cohortName, cohortIdNumber,
								"Auto-generated cohort for " + courseFullName, categoryId);
						
						if (cohortResp == null || cohortResp.isEmpty()) {
							log.error("Failed to create cohort - Response is null or empty. Course: {}, CohortName: {}", 
									courseFullName, cohortName);
							failureCount++;
							continue;
						}
						
						log.debug("Cohort creation response received - Course: {}, Response size: {}", 
								courseFullName, cohortResp.size());
					} catch (Exception cohortEx) {
						log.error("Exception while creating cohort for course '{}' (ID: {}). " +
								"CohortName: {}, Error: {}", 
								courseFullName, moodleCourseId, cohortName, cohortEx.getMessage(), cohortEx);
						failureCount++;
						continue;
					}

					Long cohortId = null;
					try {
						cohortId = ((Number) cohortResp.get(0).get("id")).longValue();
						log.debug("Extracted cohortId: {} for course: {}", cohortId, courseFullName);
					} catch (Exception idEx) {
						log.error("Failed to extract cohortId from response for course '{}'. " +
								"Response: {}, Error: {}", 
								courseFullName, cohortResp, idEx.getMessage(), idEx);
						failureCount++;
						continue;
					}

					try {
						log.debug("Linking cohort to course - CourseId: {}, CohortId: {}, RoleId: {}", 
								moodleCourseId, cohortId, moodleConfig.getDefaultRoleId());
						moodleService.linkCohortToCourse(moodleCourseId, cohortId, moodleConfig.getDefaultRoleId());
						log.debug("Successfully linked cohort to course - CourseId: {}, CohortId: {}", 
								moodleCourseId, cohortId);
					} catch (Exception linkEx) {
						log.error("Failed to link cohort to course - CourseId: {}, CohortId: {}, Error: {}", 
								moodleCourseId, cohortId, linkEx.getMessage(), linkEx);
						failureCount++;
						continue;
					}

					try {
						MoodleCourseMappingEntity mapping = new MoodleCourseMappingEntity();
						mapping.setMoodleCourseId(moodleCourseId);
						mapping.setMoodleCourseName(courseFullName);
						mapping.setSkillName(courseFullName);
						mapping.setCohortId(cohortId);
						mapping.setCohortIdnumber(cohortIdNumber);
						mapping.setActive(true);
						mapping.setProcessed(true);
						mapping.setLastSynced(LocalDateTime.now());
						mapping.setCreatedAt(LocalDateTime.now());
						mappingRepo.save(mapping);
						log.info("Cohort '{}' linked with course '{}' (ID: {}) and mapping saved", 
								cohortName, courseFullName, moodleCourseId);
						successCount++;
					} catch (Exception saveEx) {
						log.error("Failed to save course-cohort mapping - CourseId: {}, CohortId: {}, Error: {}", 
								moodleCourseId, cohortId, saveEx.getMessage(), saveEx);
						failureCount++;
					}

				} catch (ClassCastException cce) {
					log.error("ClassCastException while processing course data. Course: {}, Error: {}. " +
							"Course data: {}", 
							courseFullName != null ? courseFullName : "Unknown", 
							cce.getMessage(), course, cce);
					failureCount++;
				} catch (NullPointerException npe) {
					log.error("NullPointerException while processing course - CourseId: {}, CourseName: {}. " +
							"Error: {}", 
							moodleCourseId, courseFullName, npe.getMessage(), npe);
					failureCount++;
				} catch (Exception courseEx) {
					log.error("Unexpected error while processing course - CourseId: {}, CourseName: {}. " +
							"Error: {}", 
							moodleCourseId, courseFullName, courseEx.getMessage(), courseEx);
					failureCount++;
				}
			}

			log.info("=== Completed syncCoursesWithCategoryCohort job - Success: {}, Skipped: {}, Failed: {}, Total: {} ===", 
					successCount, skippedCount, failureCount, courses.size());

		} catch (Exception e) {
			log.error("Fatal error during course–cohort sync. Success: {}, Skipped: {}, Failed: {}. " +
					"Error: {}", 
					successCount, skippedCount, failureCount, e.getMessage(), e);
			throw new MoodleSyncException("Failed during scheduled sync", e);
		}
	}

	// TODO: Change back to "0 0 * * * *" (every 1 hour) after testing
	@Scheduled(cron = "0 0/15 * * * *") // Currently: every 15 min for testing
	public void syncMoodleAttempts() {
		log.info("=== Starting syncMoodleAttempts job ===");
		
		try {
			List<Long> moodleUserIds = moodleProfileRepo.findMoodleUserIdsByStatus(Status.QUIZ_COMPLETED);
			log.info("Found {} Moodle user IDs with QUIZ_COMPLETED status", moodleUserIds.size());

			if (moodleUserIds.isEmpty()) {
				log.info("No users to sync attempts for. Exiting syncMoodleAttempts job.");
				return;
			}

			int successCount = 0;
			int failureCount = 0;

			for (Long uid : moodleUserIds) {
				log.debug("Syncing attempts for Moodle userId: {}", uid);
				try {
					moodleService.syncUserAttemptsUsingFeign(uid, 0L);
					log.debug("Successfully synced attempts for userId: {}", uid);
					successCount++;
				} catch (Exception e) {
					log.error("Failed to sync attempts for userId: {}. Error: {}", uid, e.getMessage(), e);
					failureCount++;
				}
			}

			log.info("=== Completed syncMoodleAttempts job - Success: {}, Failed: {}, Total: {} ===", 
					successCount, failureCount, moodleUserIds.size());

		} catch (Exception e) {
			log.error("Fatal error during syncMoodleAttempts. Error: {}", e.getMessage(), e);
			throw new MoodleSyncException("Failed during syncMoodleAttempts", e);
		}
	}

}
