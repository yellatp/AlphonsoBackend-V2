package com.alphonso.Interviewer_Service.ServiceImp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alphonso.Interviewer_Service.Entity.AvailabilitySlot;
import com.alphonso.Interviewer_Service.Entity.Interview;
import com.alphonso.Interviewer_Service.FeignClient.ProfileClient;
import com.alphonso.Interviewer_Service.Repository.AvailabilitySlotRepository;
import com.alphonso.Interviewer_Service.Repository.InterviewRepository;
import com.alphonso.Interviewer_Service.ResponseDTO.CandidateDetailsForInterview;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewSchedulerService {

	private final AvailabilitySlotRepository slotRepo;
	private final InterviewRepository interviewRepo;
	private final ProfileClient profileClient;
	private final JitsiMeetService jitsiMeetService;
	private final EmailService emailService;
	private final ProfileCircuitBreakerService profileCircuitBreaker;
	private final Random random = new Random();

	@Value("${google.timezone:UTC}")
	private String timezone;

	@Transactional
	public void scheduleForQuizPassedCandidates() {
		log.info("Fetching quiz passed candidates for interview scheduling");

	    List<CandidateDetailsForInterview> candidates;

	    try {
	        candidates = profileCircuitBreaker.getQuizPassedCandidates();
	        log.info("Retrieved {} quiz passed candidates from profile service", 
	        		candidates != null ? candidates.size() : 0);

	    } catch (Exception ex) {
	        log.error("Failed to fetch candidates from profile service: {}", ex.getMessage(), ex);
	        return; 
	    }

	    if (candidates == null || candidates.isEmpty()) {
	        log.info("No quiz passed candidates available for scheduling");
	        return;
	    }

	    int successCount = 0;
	    int failureCount = 0;
	    int noSlotsCount = 0;

	    for (CandidateDetailsForInterview candidate : candidates) {
	        try {
	        	log.info("Attempting to schedule interview for candidate - ProfileId: {}, Email: {}, Domain: {}", 
	        			candidate.getProfileId(), candidate.getEmail(), candidate.getDomainName());
	            boolean scheduled = scheduleForCandidate(
	                    candidate.getProfileId(),
	                    candidate.getDomainName(),
	                    candidate.getEmail(),
	                    candidate.getFirstName()
	            );
	            if (scheduled) {
	            	successCount++;
	            	log.info("Successfully scheduled interview for candidate - ProfileId: {}", candidate.getProfileId());
	            } else {
	            	noSlotsCount++;
	            	log.warn("Could not schedule interview for candidate - ProfileId: {} - No available slots", candidate.getProfileId());
	            }
	        } catch (Exception ex) {
	        	failureCount++;
	            log.error("Failed scheduling for candidate - ProfileId: {}, Email: {}. Error: {}", 
	            		candidate.getProfileId(), candidate.getEmail(), ex.getMessage(), ex);
	        }
	    }
	    
	    log.info("Interview scheduling completed - Success: {}, No Slots: {}, Failed: {}, Total: {}", 
	    		successCount, noSlotsCount, failureCount, candidates.size());
	}

	@Transactional
	public boolean scheduleForCandidate(String profileId, String domain, String email, String candidateName) {
		log.info("Scheduling interview for candidate - ProfileId: {}, Domain: {}, Email: {}", 
				profileId, domain, email);

		try {
			List<AvailabilitySlot> freeSlots = slotRepo.findFreeSlotsByDomainOrdered(domain, LocalDateTime.now());
			log.info("Found {} free slots for domain: {}", 
					freeSlots != null ? freeSlots.size() : 0, domain);

			if (freeSlots == null || freeSlots.isEmpty()) {
				log.warn("No free slots available for domain: {}. Keeping candidate status as QUIZ_PASSED. ProfileId: {}", 
						domain, profileId);
				profileClient.updateAssessmentStatus(profileId, "QUIZ_PASSED");
				return false; // Return false to indicate no slots available
			}

			LocalDateTime earliestStart = freeSlots.get(0).getStart();

			List<AvailabilitySlot> earliest = freeSlots.stream().filter(s -> s.getStart().equals(earliestStart))
					.collect(Collectors.toList());

			AvailabilitySlot chosen = earliest.get(random.nextInt(earliest.size()));

			chosen.setStatus(AvailabilitySlot.Status.BOOKED);
			slotRepo.save(chosen);

			List<AvailabilitySlot> overlapping = slotRepo.findOverlappingFreeSlotsForInterviewer(
					chosen.getInterviewer().getId(), chosen.getStart(), chosen.getEnd());

			for (AvailabilitySlot o : overlapping) {
				o.setStatus(AvailabilitySlot.Status.REMOVED);
			}
			slotRepo.saveAll(overlapping);

			String interviewerEmail = chosen.getInterviewer().getEmail();
			// String interviewerName=chosen.getInterviewer().getName();

			Interview interview = new Interview();
			interview.setCandidateProfileId(profileId);
			interview.setInterviewerId(chosen.getInterviewer().getId());
			interview.setCandidateEmail(email);
			interview.setInterviewerEmail(interviewerEmail);
			interview.setStartTime(chosen.getStart());
			interview.setEndTime(chosen.getEnd());
			interview.setInterviewId(generateUniqueInterviewCode());
			interview.setDomain(domain);

			Interview saved = interviewRepo.save(interview);
			log.info("Interview created - InterviewId: {}, ProfileId: {}, StartTime: {}, EndTime: {}", 
					saved.getInterviewId(), profileId, saved.getStartTime(), saved.getEndTime());

			profileClient.updateAssessmentStatus(profileId, "WAITING_T1");
			log.info("Updated assessment status to WAITING_T1 for ProfileId: {}", profileId);

			String meetUrl = jitsiMeetService.generateMeetUrl(saved.getInterviewId());

			saved.setMeetUrl(meetUrl);
			interviewRepo.save(saved);
			log.info("Meet URL generated and saved - InterviewId: {}, MeetUrl: {}", saved.getInterviewId(), meetUrl);

			if (candidateName == null)
				candidateName = "Candidate";

			String interviewerName = (chosen.getInterviewer().getName() != null) ? chosen.getInterviewer().getName()
					: "Interviewer";

			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			String startStr = saved.getStartTime().format(fmt);
			String endStr = saved.getEndTime().format(fmt);

			try {
				emailService.sendCandidateBookedEmail(saved.getCandidateEmail(), candidateName, saved.getInterviewId(), startStr,
						endStr, meetUrl);
			} catch (Exception e) {
				log.error("Failed to send candidate email for interview {}: {}", saved.getInterviewId(), e.getMessage(),
						e);
			}

			try {
				emailService.sendInterviewerScheduledEmail(saved.getInterviewerEmail(), interviewerName, saved.getInterviewId(),
						startStr, endStr, meetUrl);
			} catch (Exception e) {
				log.error("Failed to send interviewer email for interview {}: {}", saved.getInterviewId(),
						e.getMessage(), e);
			}

			return true; // Return true to indicate successful scheduling

		} catch (Exception ex) {

			try {
				profileClient.updateAssessmentStatus(profileId, "QUIZ_PASSED");
			} catch (Exception ignore) {
				log.warn("Failed updating profile back to QUIZ_PASSED for {}: {}", profileId, ignore.getMessage());
			}

			throw new RuntimeException("Failed to schedule interview for candidate " + profileId, ex);
		}
	}

	private String generateUniqueInterviewCode() {
		int attempts = 0;

		while (attempts++ < 10) {

			int number = 1000 + random.nextInt(9000);

			String code = "INT" + number;

			if (!interviewRepo.existsByInterviewId(code)) {
				return code;
			}
		}

		return "INT" + ((int) (System.currentTimeMillis() % 9000) + 1000);
	}

}