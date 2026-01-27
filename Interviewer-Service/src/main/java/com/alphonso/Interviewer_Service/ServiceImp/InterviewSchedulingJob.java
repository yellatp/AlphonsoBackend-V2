package com.alphonso.Interviewer_Service.ServiceImp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.alphonso.Interviewer_Service.Entity.InterviewerDetails;
import com.alphonso.Interviewer_Service.Repository.InterviewerRepository;
import com.alphonso.Interviewer_Service.RequestDTO.AppProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewSchedulingJob {

	private final InterviewSchedulerService schedulerService;
	private final InterviewerRepository interviewerRepo;
	private final EmailService mailService;
	private final AppProperties appProperties;

	// TODO: Change back to "0 0 * * * *" (every 1 hour) after testing
	@Scheduled(cron = "0 0/15 * * * *") // Currently: every 15 min for testing
	public void runForQuizPassedCandidates() {
		log.info("=== Starting interview scheduling job for quiz passed candidates ===");
		try {
			schedulerService.scheduleForQuizPassedCandidates();
			log.info("=== Completed interview scheduling job ===");
		} catch (Exception ex) {
			log.error("=== Interview scheduling job failed: {} ===", ex.getMessage(), ex);
		}
	}

	@Scheduled(cron = "0 0 15 * * *", zone = "Asia/Kolkata")
	public void runMorningReminder() {
		maybeSendMonthlyReminders("morning");
	}

	@Scheduled(cron = "0 0 16 * * *", zone = "Asia/Kolkata")
	public void runAfternoonReminder() {
		maybeSendMonthlyReminders("afternoon");
	}

	private void maybeSendMonthlyReminders(String runLabel) {
		ZoneId zone = ZoneId.of(appProperties.getTimezone());
		LocalDate today = LocalDate.now(zone);
		LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());

		log.info("Reminder job '{}' checking date {} (lastDay={})", runLabel, today, lastDay);

		if (!today.equals(lastDay)) {
			log.debug("Not last day of month -> skipping reminders.");
			return;
		}

		List<InterviewerDetails> interviewers = interviewerRepo.findAllForReminders();
		if (interviewers == null || interviewers.isEmpty()) {
			log.info("No interviewers to notify.");
			return;
		}

		for (InterviewerDetails i : interviewers) {
			try {
				if (i.getEmail() == null || i.getEmail().isBlank()) {
					log.warn("Interviewer {} has no email - skipping", i.getId());
					continue;
				}

				String name = i.getName() != null ? i.getName() : "Interviewer";

				if ("morning".equalsIgnoreCase(runLabel)) {
					mailService.sendMorningReminder(i.getEmail(), name);
				} else {
					mailService.sendAfternoonReminder(i.getEmail(), name);
				}

			} catch (Exception ex) {
				log.error("Failed to notify interviewer id {}: {}", i.getId(), ex.getMessage(), ex);
			}
		}

		log.info("Reminder job '{}' completed. Sent {} emails (attempted).", runLabel, interviewers.size());
	}

}
