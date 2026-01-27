package com.alphonso.moodle_employer_service.Moodle.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentAttemptCategoryView;
import com.alphonso.moodle_employer_service.Moodle.Exception.MoodleSyncException.AssessmentResultNotFoundException;
import com.alphonso.moodle_employer_service.Moodle.Repository.AssessmentAttemptCategoryViewRepository;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.AssessmentResultResponse;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.AttemptDto;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.CategoryScoreDto;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.CategoryScoreResponse;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.UserAssessmentReportDto;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssessmentReportService {

	private final AssessmentAttemptCategoryViewRepository viewRepo;

	public AssessmentReportService(AssessmentAttemptCategoryViewRepository viewRepo) {
		this.viewRepo = viewRepo;
	}

	public UserAssessmentReportDto getUserReport(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email must not be empty");
		}

		List<AssessmentAttemptCategoryView> rows = viewRepo.findByEmailIgnoreCase(email);

		if (rows.isEmpty()) {
			UserAssessmentReportDto empty = new UserAssessmentReportDto();
			empty.setEmail(email);
			empty.setAttempts(Collections.emptyList());
			empty.setTotalScore(0.0);
			return empty;
		}

		Map<Long, List<AssessmentAttemptCategoryView>> byAttempt = rows.stream().collect(Collectors
				.groupingBy(AssessmentAttemptCategoryView::getAttemptId, LinkedHashMap::new, Collectors.toList()));

		List<AttemptDto> attemptDtos = new ArrayList<>();
		double totalScoreAcc = 0.0;

		for (List<AssessmentAttemptCategoryView> attemptRows : byAttempt.values()) {

			AssessmentAttemptCategoryView sample = attemptRows.get(0);

			AttemptDto dto = new AttemptDto();
			dto.setAttemptId(sample.getAttemptId());
			dto.setQuizId(sample.getQuizId());
			dto.setAttemptDate(sample.getAttemptDate());
			dto.setScore(sample.getScore());

			List<CategoryScoreDto> categoryDtos = attemptRows.stream().map(v -> {
				CategoryScoreDto c = new CategoryScoreDto();
				c.setCategoryId(v.getCategoryId());
				c.setCategoryName(v.getCategoryName());
				c.setEarned(v.getEarned());
				c.setPossible(v.getPossible());
				c.setPercentage(v.getPercentage());
				c.setQuestionCount(v.getQuestionCount());
				return c;
			}).collect(Collectors.toList());

			dto.setCategories(categoryDtos);
			attemptDtos.add(dto);

			if (dto.getScore() != null) {
				totalScoreAcc += dto.getScore();
			}
		}

		UserAssessmentReportDto out = new UserAssessmentReportDto();
		out.setProfileId(rows.get(0).getProfileId());
		out.setEmail(email);
		out.setMoodleUserId(rows.get(0).getMoodleUserId());
		out.setAttempts(attemptDtos);
		out.setTotalScore(totalScoreAcc);

		return out;
	}

	public Page<UserAssessmentReportDto> getAllReports(Pageable pageable, LocalDateTime since) {

		List<String> distinctProfiles = viewRepo.findDistinctProfileIdsSince(since);

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), distinctProfiles.size());
		List<String> pageProfiles = start >= distinctProfiles.size() ? Collections.emptyList()
				: distinctProfiles.subList(start, end);

		List<UserAssessmentReportDto> list = new ArrayList<>(pageProfiles.size());
		for (String pid : pageProfiles) {
			List<AssessmentAttemptCategoryView> rows = viewRepo.findByProfileId(pid);

			if (rows == null || rows.isEmpty()) {
				UserAssessmentReportDto empty = new UserAssessmentReportDto();
				empty.setProfileId(pid);
				empty.setEmail(null);
				empty.setMoodleUserId(null);
				empty.setAttempts(Collections.emptyList());
				empty.setTotalScore(0.0);
				list.add(empty);
			} else {
				String email = rows.get(0).getEmail();
				list.add(buildReportFromRows(rows, email));
			}
		}

		return new PageImpl<>(list, pageable, distinctProfiles.size());
	}

	private UserAssessmentReportDto buildReportFromRows(List<AssessmentAttemptCategoryView> rows,
			String returnedEmail) {

		Map<Long, List<AssessmentAttemptCategoryView>> byAttempt = rows.stream().collect(Collectors
				.groupingBy(AssessmentAttemptCategoryView::getAttemptId, LinkedHashMap::new, Collectors.toList()));

		List<AttemptDto> attemptDtos = new ArrayList<>(byAttempt.size());
		double totalScoreAcc = 0.0;

		for (List<AssessmentAttemptCategoryView> attemptRows : byAttempt.values()) {
			AssessmentAttemptCategoryView sample = attemptRows.get(0);

			AttemptDto dto = new AttemptDto();
			dto.setAttemptId(sample.getAttemptId());
			dto.setQuizId(sample.getQuizId());
			dto.setAttemptDate(sample.getAttemptDate());
			dto.setScore(sample.getScore());

			List<CategoryScoreDto> cats = attemptRows.stream().map(v -> {
				CategoryScoreDto c = new CategoryScoreDto();
				c.setCategoryId(v.getCategoryId());
				c.setCategoryName(v.getCategoryName());
				c.setEarned(v.getEarned());
				c.setPossible(v.getPossible());
				c.setPercentage(v.getPercentage());
				c.setQuestionCount(v.getQuestionCount());
				return c;
			}).collect(Collectors.toList());

			dto.setCategories(cats);
			attemptDtos.add(dto);

			if (dto.getScore() != null)
				totalScoreAcc += dto.getScore();
		}

		UserAssessmentReportDto out = new UserAssessmentReportDto();
		out.setProfileId(rows.get(0).getProfileId());
		out.setEmail(returnedEmail != null ? returnedEmail : rows.get(0).getEmail());
		out.setMoodleUserId(rows.get(0).getMoodleUserId());
		out.setAttempts(attemptDtos);
		out.setTotalScore(attemptDtos.isEmpty() ? 0.0 : totalScoreAcc);

		return out;
	}
	
	@Transactional
    public AssessmentResultResponse getAssessmentResult(String profileId) {

        try {
            List<AssessmentAttemptCategoryView> rows =
            		viewRepo.findByProfileIdOrderByCategoryIdAsc(profileId);

            if (rows == null || rows.isEmpty()) {
                throw new AssessmentResultNotFoundException(
                        "No assessment results found for profileId: " + profileId
                );
            }
            
            Double overallPercentage = rows.get(0).getScore();
            if (overallPercentage == null) {
                overallPercentage = 0.0;
            }

            List<CategoryScoreResponse> categoryScores = rows.stream()
                    .map(r -> CategoryScoreResponse.builder()
                            .categoryName(r.getCategoryName())
                            .percentage(r.getPercentage() != null ? r.getPercentage() : 0.0)
                            .build())
                    .collect(Collectors.toList());

            return AssessmentResultResponse.builder()
                    .profileId(profileId)
                    .moodlePercentage(overallPercentage)
                    .categoryScores(categoryScores)
                    .build();

        } catch (AssessmentResultNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error while fetching assessment results", ex);
        }
    }
	
}
