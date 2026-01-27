package com.alphonso.Interviewer_Service.ServiceImp;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alphonso.Interviewer_Service.Entity.AvailabilitySlot;
import com.alphonso.Interviewer_Service.Entity.Feedback;
import com.alphonso.Interviewer_Service.Entity.Interview;
import com.alphonso.Interviewer_Service.Entity.InterviewerDetails;
import com.alphonso.Interviewer_Service.Entity.SkillSetDetails;
import com.alphonso.Interviewer_Service.Exception.InterviewerServiceException.BadRequestException;
import com.alphonso.Interviewer_Service.Exception.InterviewerServiceException.InterviewResultNotFound;
import com.alphonso.Interviewer_Service.Exception.InterviewerServiceException.UserNotFoundException;
import com.alphonso.Interviewer_Service.Repository.AvailabilitySlotRepository;
import com.alphonso.Interviewer_Service.Repository.FeedbackRepository;
import com.alphonso.Interviewer_Service.Repository.InterviewRepository;
import com.alphonso.Interviewer_Service.Repository.InterviewerRepository;
import com.alphonso.Interviewer_Service.Repository.SkillRepository;
import com.alphonso.Interviewer_Service.RequestDTO.DateSlots;
import com.alphonso.Interviewer_Service.RequestDTO.FeedbackRequest;
import com.alphonso.Interviewer_Service.RequestDTO.MonthlyAvailabilityRequest;
import com.alphonso.Interviewer_Service.RequestDTO.RadarChartDto;
import com.alphonso.Interviewer_Service.RequestDTO.TimeSlot;
import com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDetailsResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.DateResult;
import com.alphonso.Interviewer_Service.ResponseDTO.FeedbackResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewResultResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewSummaryDto;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDto;
import com.alphonso.Interviewer_Service.Service.IInterviewerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewerServiceLayer implements IInterviewerService {

	private final InterviewerRepository interviewerRepo;
	private final InterviewRepository interviewRepo;
	private final AvailabilitySlotRepository slotRepo;
	private final SkillRepository skillRepo;
	private final FeedbackRepository feedbackRepo;
	private final ProfileCircuitBreakerService profileCircuitBreaker;

	@Transactional
	public List<DateResult> addMonthlyAvailability(MonthlyAvailabilityRequest req, String callerEmail) {

		if (callerEmail == null || callerEmail.isBlank()) {
			throw new BadRequestException("callerEmail required");
		}

		InterviewerDetails interviewer = interviewerRepo.findByEmail(callerEmail).orElseGet(() -> {

			InterviewerDto dto;

			try {
				dto = profileCircuitBreaker.getByEmail(callerEmail);

				if (dto == null || dto.getEmail() == null) {
					throw new RuntimeException(
							"Profile service is unavailable / interviewer not found for email: " + callerEmail);
				}

			} catch (Exception e) {
				throw new RuntimeException("Profile service error for " + callerEmail, e);
			}

			if (dto.getEmail() == null) {
				throw new UserNotFoundException(
						"Interviewer profile not found in Profile Service for email: " + callerEmail);
			}

			InterviewerDetails newInterviewer = new InterviewerDetails();
			newInterviewer.setEmail(dto.getEmail());
			newInterviewer.setName((dto.getFirstName() == null ? "" : dto.getFirstName())
					+ (dto.getLastName() == null ? "" : " " + dto.getLastName()));
			newInterviewer.setProfileId(dto.getProfileId());
			newInterviewer.setCreatedAt(LocalDateTime.now());

			try {
				return interviewerRepo.save(newInterviewer);
			} catch (DataIntegrityViolationException dex) {
				return interviewerRepo.findByEmail(callerEmail)
						.orElseThrow(() -> new RuntimeException("Concurrent create failed"));
			}
		});

		if (req.getSkillIds() != null && !req.getSkillIds().isEmpty()) {
			List<SkillSetDetails> foundSkills = skillRepo.findAllByIdIn(req.getSkillIds());
			interviewer.getSkills().addAll(foundSkills);
			interviewerRepo.save(interviewer);
		}

		List<DateResult> results = new ArrayList<>();

		if (req.getDates() == null || req.getDates().isEmpty()) {
			return results;
		}

		for (DateSlots ds : req.getDates()) {

			if (ds == null || ds.getDate() == null) {
				results.add(new DateResult(null, "invalid_date", Collections.emptyList(), List.of("Invalid date")));
				continue;
			}

			LocalDate date = ds.getDate();

			List<String> conflicts = new ArrayList<>();
			List<String> invalids = new ArrayList<>();

			if (ds.getSlots() == null || ds.getSlots().isEmpty()) {
				continue;
			}

			for (TimeSlot ts : ds.getSlots()) {

				if (ts == null || ts.getStart() == null || ts.getEnd() == null) {
					invalids.add("invalid-slot-format");
					continue;
				}

				LocalTime st;
				LocalTime en;

				try {
					st = LocalTime.parse(ts.getStart());
					en = LocalTime.parse(ts.getEnd());
				} catch (DateTimeParseException ex) {
					invalids.add(ts.getStart() + "-" + ts.getEnd());
					continue;
				}

				if (!st.isBefore(en)) {
					invalids.add(ts.getStart() + "-" + ts.getEnd());
					continue;
				}

				LocalDateTime startDt = LocalDateTime.of(date, st);
				LocalDateTime endDt = LocalDateTime.of(date, en);

				while (startDt.plusHours(1).isBefore(endDt) || startDt.plusHours(1).isEqual(endDt)) {

					LocalDateTime slotEnd = startDt.plusHours(1);

					boolean overlap = slotRepo.existsOverlap(interviewer.getId(), startDt, slotEnd);

					if (overlap) {
						conflicts.add(startDt.toLocalTime() + "-" + slotEnd.toLocalTime());
					} else {
						AvailabilitySlot slot = new AvailabilitySlot();
						slot.setInterviewer(interviewer);
						slot.setStart(startDt);
						slot.setEnd(slotEnd);
						slot.setStatus(AvailabilitySlot.Status.FREE);
						slotRepo.save(slot);
					}

					startDt = slotEnd;
				}
			}

			if (!conflicts.isEmpty()) {
				results.add(new DateResult(date, "conflict", Collections.emptyList(), conflicts));
			}

			if (!invalids.isEmpty()) {
				results.add(new DateResult(date, "invalid", Collections.emptyList(), invalids));
			}
		}

		if (results.isEmpty()) {
			return Collections.emptyList();
		}

		return results;
	}

	@Transactional(readOnly = true)
	public List<AvailabilitySlotResponse> getCurrentMonthAvailability(String email) {

		try {
			if (email == null || email.isBlank()) {
				throw new BadRequestException("Email is required");
			}

			InterviewerDetails interviewer = interviewerRepo.findByEmail(email)
					.orElseThrow(() -> new UserNotFoundException("Interviewer not found for email: " + email));

			LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
			LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);

			LocalDateTime from = firstDayOfMonth.atStartOfDay();
			LocalDateTime to = firstDayOfNextMonth.atStartOfDay();

			List<AvailabilitySlotResponse> slots = slotRepo.findAvailabilityResponseForMonth(interviewer, from, to);

			return (slots == null) ? Collections.emptyList() : slots;

		} catch (BadRequestException | UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching availability for email: " + email, e);
		}
	}

	public List<InterviewSummaryDto> getTodayInterviews(String email) {
		try {
			if (email == null || email.isBlank()) {
				throw new BadRequestException("Email is required");
			}

			String normalizedEmail = email.trim().toLowerCase();
			log.info("Fetching today's interviews for email: {}", normalizedEmail);
			
			// Query by interviewerEmail first (for interviewers)
			List<Interview> interviewerList = interviewRepo.findTodayInterviews(normalizedEmail);
			
			// Query by candidateEmail (for candidates)
			List<Interview> candidateList = interviewRepo.findTodayInterviewsByCandidateEmail(normalizedEmail);
			
			// Combine results (using a Set to avoid duplicates if someone is both interviewer and candidate)
			Set<Interview> combinedSet = new HashSet<>(interviewerList);
			combinedSet.addAll(candidateList);
			List<Interview> list = new ArrayList<>(combinedSet);
			
			log.info("Found {} interviews today for email: {} ({} as interviewer, {} as candidate)", 
				list.size(), normalizedEmail, interviewerList.size(), candidateList.size());

			return list.stream().map(i -> new InterviewSummaryDto(i.getInterviewId(), i.getDomain(), i.getStartTime(),
					i.getEndTime(), i.getMeetUrl())).collect(Collectors.toList());

		} catch (BadRequestException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Failed to fetch today's interviews for email {}: {}", email, ex.getMessage(), ex);
			throw new RuntimeException("Unable to fetch today's interviews. Please try again later.", ex);
		}
	}

	public List<InterviewSummaryDto> getUpcomingInterviews(String email) {
		try {
			if (email == null || email.isBlank()) {
				throw new BadRequestException("Email is required");
			}

			String normalizedEmail = email.trim().toLowerCase();
			log.info("Fetching upcoming interviews for email: {}", normalizedEmail);
			
			// Query by interviewerEmail first (for interviewers)
			List<Interview> interviewerList = interviewRepo.findUpcomingInterviews(normalizedEmail);
			
			// Query by candidateEmail (for candidates)
			List<Interview> candidateList = interviewRepo.findUpcomingInterviewsByCandidateEmail(normalizedEmail);
			
			// Combine results (using a Set to avoid duplicates if someone is both interviewer and candidate)
			Set<Interview> combinedSet = new HashSet<>(interviewerList);
			combinedSet.addAll(candidateList);
			List<Interview> list = new ArrayList<>(combinedSet);
			
			log.info("Found {} upcoming interviews for email: {} ({} as interviewer, {} as candidate)", 
				list.size(), normalizedEmail, interviewerList.size(), candidateList.size());
			
			if (list.isEmpty()) {
				// Log all interviews for this email to help debug
				List<Interview> allInterviews = interviewRepo.findAll().stream()
					.filter(i -> (i.getInterviewerEmail() != null && 
					            i.getInterviewerEmail().trim().toLowerCase().equals(normalizedEmail)) ||
					            (i.getCandidateEmail() != null && 
					            i.getCandidateEmail().trim().toLowerCase().equals(normalizedEmail)))
					.collect(Collectors.toList());
				log.info("Total interviews found for email {}: {}", normalizedEmail, allInterviews.size());
				if (!allInterviews.isEmpty()) {
					log.info("Sample interview startTime: {}, Current timestamp: {}", 
						allInterviews.get(0).getStartTime(), LocalDateTime.now());
				}
			}

			return list.stream().map(i -> new InterviewSummaryDto(i.getInterviewId(), i.getDomain(), i.getStartTime(),
					i.getEndTime(), i.getMeetUrl())).collect(Collectors.toList());

		} catch (BadRequestException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Failed to fetch upcoming interviews for email {}: {}", email, ex.getMessage(), ex);
			throw new RuntimeException("Unable to fetch upcoming interviews. Please try again later.", ex);
		}
	}

	@Transactional
	public FeedbackResponse saveOrUpdateFeedback(String interviewId, FeedbackRequest req) {

		validateRating(req.getAnalytical(), "analytical");
		validateRating(req.getTechnical(), "technical");
		validateRating(req.getDesign(), "design");
		validateRating(req.getExecution(), "execution");
		validateRating(req.getCommunication(), "communication");
		validateRating(req.getCollaboration(), "collaboration");
		validateRating(req.getAdaptability(), "adaptability");

		Interview interview = interviewRepo.findByInterviewId(interviewId)
				.orElseThrow(() -> new RuntimeException("Interview not found with interviewId: " + interviewId));

		Optional<Feedback> maybeExisting = Optional.empty();
		if (interviewId != null) {
			maybeExisting = feedbackRepo.findByInterviewId(interviewId);
		}

		Feedback fb = maybeExisting.orElseGet(Feedback::new);

		fb.setInterviewId(interview.getInterviewId());
		fb.setCandidateProfileId(interview.getCandidateProfileId());
		fb.setInterviewerId(String.valueOf(interview.getInterviewerId()));
		fb.setAnalytical(req.getAnalytical());
		fb.setTechnical(req.getTechnical());
		fb.setDesign(req.getDesign());
		fb.setExecution(req.getExecution());
		fb.setCommunication(req.getCommunication());
		fb.setCollaboration(req.getCollaboration());
		fb.setAdaptability(req.getAdaptability());

		boolean allAboveThree = req.getAnalytical() >= 3 && req.getTechnical() >= 3 && req.getDesign() >= 3
				&& req.getExecution() >= 3 && req.getCommunication() >= 3 && req.getCollaboration() >= 3
				&& req.getAdaptability() >= 3;

		fb.setRecommendation(allAboveThree ? Feedback.Recommendation.YES : Feedback.Recommendation.NO);
		fb.setNotes(req.getNotes());

		String candidateProfileId = interview.getCandidateProfileId();
		String finalStatus = (fb.getRecommendation() == Feedback.Recommendation.YES) ? "COMPLETED_T1" : "REJECTED_T1";

		try {
			profileCircuitBreaker.updateAssessmentStatus(candidateProfileId, finalStatus);

			Feedback saved = feedbackRepo.save(fb);

			return toResponse(saved);

		} catch (Exception e) {
			try {
				profileCircuitBreaker.updateAssessmentStatus(candidateProfileId, "WAITING_T1");
			} catch (Exception ex) {
			}

			throw new RuntimeException(
					"Failed to save feedback / update assessment status for interviewId: " + interviewId, e);
		}
	}

	public List<FeedbackResponse> getFeedbackForCandidate(String candidateProfileId) {
		return feedbackRepo.findByCandidateProfileIdOrderByCreatedAtDesc(candidateProfileId).stream()
				.map(InterviewerServiceLayer::toResponse).toList();
	}

	public Optional<FeedbackResponse> getLatestForCandidate(String candidateProfileId) {
		List<Feedback> list = feedbackRepo.findByCandidateProfileIdOrderByCreatedAtDesc(candidateProfileId);
		if (list.isEmpty())
			return Optional.empty();
		return Optional.of(InterviewerServiceLayer.toResponse(list.get(0)));
	}

	public RadarChartDto toRadarDto(Long feedbackId) {
		Feedback fb = feedbackRepo.findById(feedbackId)
				.orElseThrow(() -> new NoSuchElementException("Feedback not found: " + feedbackId));
		String[] labels = new String[] { "Analytical", "Technical", "Design", "Execution", "Communication",
				"Collaboration", "Adaptability" };
		Double[] values = new Double[] { fb.getAnalytical().doubleValue(), fb.getTechnical().doubleValue(),
				fb.getDesign().doubleValue(), fb.getExecution().doubleValue(), fb.getCommunication().doubleValue(),
				fb.getCollaboration().doubleValue(), fb.getAdaptability().doubleValue() };
		return new RadarChartDto(labels, values);
	}

	private static FeedbackResponse toResponse(Feedback fb) {
		FeedbackResponse r = new FeedbackResponse();
		r.setId(fb.getId());
		r.setCandidateProfileId(fb.getCandidateProfileId());
		r.setInterviewerId(fb.getInterviewerId());
		r.setInterviewId(fb.getInterviewId());
		Map<String, Integer> ratings = new LinkedHashMap<>();
		ratings.put("Analytical", fb.getAnalytical());
		ratings.put("Technical", fb.getTechnical());
		ratings.put("Design", fb.getDesign());
		ratings.put("Execution", fb.getExecution());
		ratings.put("Communication", fb.getCommunication());
		ratings.put("Collaboration", fb.getCollaboration());
		ratings.put("Adaptability", fb.getAdaptability());
		r.setRatings(ratings);
		r.setRecommendation(fb.getRecommendation());
		r.setNotes(fb.getNotes());
		r.setCreatedAt(fb.getCreatedAt());
		return r;
	}

	private static void validateRating(Integer v, String name) {
		if (v == null || v < 1 || v > 5) {
			throw new IllegalArgumentException("Rating " + name + " must be 1..5");
		}
	}

	@Transactional(readOnly = true)
	public InterviewResultResponse getInterviewResult(String candidateProfileId) {

		try {
			Feedback feedback = feedbackRepo.findTopByCandidateProfileIdOrderByCreatedAtDesc(candidateProfileId)
					.orElseThrow(() -> new InterviewResultNotFound(
							"No interview feedback found for profileId: " + candidateProfileId));

			double adaptability = toPercentage(feedback.getAdaptability());
			double analytical = toPercentage(feedback.getAnalytical());
			double collaboration = toPercentage(feedback.getCollaboration());
			double communication = toPercentage(feedback.getCommunication());
			double design = toPercentage(feedback.getDesign());
			double execution = toPercentage(feedback.getExecution());
			double technical = toPercentage(feedback.getTechnical());

			int totalScore = feedback.getAdaptability() + feedback.getAnalytical() + feedback.getCollaboration()
					+ feedback.getCommunication() + feedback.getDesign() + feedback.getExecution()
					+ feedback.getTechnical();

			double overallPercentage = Math.round(((totalScore * 100.0) / 35.0) * 100.0) / 100.0;

			return InterviewResultResponse.builder().candidateProfileId(candidateProfileId)
					.interviewPercentage(overallPercentage)

					.adaptabilityPercentage(adaptability).analyticalPercentage(analytical)
					.collaborationPercentage(collaboration).communicationPercentage(communication)
					.designPercentage(design).executionPercentage(execution).technicalPercentage(technical).build();

		} catch (InterviewResultNotFound ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Error while fetching interview result", ex);
		}
	}

	private double toPercentage(Integer value) {
		if (value == null)
			return 0.0;
		return Math.round(((value * 100.0) / 5.0) * 100.0) / 100.0;
	}

	@Transactional(readOnly = true)
	public List<SkillSetDetails> getAllSkills() {
		log.info("Fetching all skills from database");
		List<SkillSetDetails> skills = skillRepo.findAll();
		log.info("Found {} skills in database", skills.size());
		return skills;
	}

	@Transactional(readOnly = true)
	public InterviewerDetailsResponse getInterviewerDetails(String email) {
		try {
			if (email == null || email.isBlank()) {
				throw new BadRequestException("Email is required");
			}

			InterviewerDetails interviewer = interviewerRepo.findByEmail(email.trim().toLowerCase())
					.orElseThrow(() -> new UserNotFoundException("Interviewer not found for email: " + email));

			// Convert entity to response DTO
			InterviewerDetailsResponse response = new InterviewerDetailsResponse();
			response.setId(interviewer.getId());
			response.setProfileId(interviewer.getProfileId());
			response.setEmail(interviewer.getEmail());
			response.setName(interviewer.getName());
			response.setSkills(interviewer.getSkills()); // Skills are already loaded via @ManyToMany(fetch = FetchType.EAGER)
			response.setCreatedAt(interviewer.getCreatedAt());

			log.info("Returning interviewer details for {} with {} skills", email, interviewer.getSkills().size());
			return response;

		} catch (BadRequestException | UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching interviewer details for email: " + email, e);
		}
	}
}
