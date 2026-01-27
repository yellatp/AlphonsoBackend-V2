package com.alphonso.moodle_employer_service.Moodle.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillQbankCategoryMap;
import com.alphonso.moodle_employer_service.Moodle.Repository.CourseRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.SkillQbankCategoryMapRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.SkillRepository;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.ContextInfo;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.QCatInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodleAssessmentService {

	private final CourseRepository courseRepo;
	private final MoodleApiService moodleService;
	private final SkillRepository skillRepo;
	private final SkillQbankCategoryMapRepository mapRepo;

	@Transactional
	public void bootstrapQcats(String hostCourseShortname) {
		log.info("Starting qcat bootstrap for hostCourseShortname='{}'", hostCourseShortname);

		Long hostCourseId = courseRepo.findIdByShortName(hostCourseShortname);
		if (hostCourseId == null) {
			throw new RuntimeException("Invalid host course shortname: " + hostCourseShortname);
		}

		ContextInfo ctx = moodleService.getContextForCourse(hostCourseId);
		log.info("Host course {} context: {}", hostCourseId, ctx);

		List<QCatInfo> qcats = moodleService.getQbankCategories(hostCourseId);
		if (qcats == null || qcats.isEmpty()) {
			log.warn("No qbank categories found for host course {}", hostCourseId);
			return;
		}

		qcats.stream().filter(q -> q.name() != null && !q.name().trim().isEmpty())
				.collect(Collectors.toMap(q -> normalize(q.name()), q -> q, (a, b) -> a));

		List<String> skills = skillRepo.findAllSkillNames();
		if (skills == null || skills.isEmpty()) {
			log.warn("No skills found in local repo");
			return;
		}

		for (String skill : skills) {
			try {
				Optional<QCatInfo> matched = matchCategoryForSkill(skill, qcats);
				if (matched.isPresent()) {
					QCatInfo q = matched.get();
					upsertMapping(skill, q, hostCourseId);
					log.info("Mapped skill '{}' -> qcat {} (ctx {})", skill, q.id(), q.contextid());
				} else {
					log.warn("No qcat match for skill '{}'", skill);
				}
			} catch (Exception e) {
				log.error("Error matching skill '{}': {}", skill, e.getMessage(), e);
			}
		}

		log.info("Completed qcat bootstrap for hostCourseShortname='{}'", hostCourseShortname);
	}

	private void upsertMapping(String skillName, QCatInfo q, Long hostCourseId) {
		Optional<SkillQbankCategoryMap> existingOpt = mapRepo.findBySkillNameIgnoreCase(skillName);
		SkillQbankCategoryMap m = existingOpt.orElseGet(SkillQbankCategoryMap::new);

		boolean isNew = existingOpt.isEmpty();
		boolean changed = false;

		if (isNew) {
			m.setSkillName(skillName);
			m.setQcatId(q.id());
			m.setHostCourseId(hostCourseId);
			m.setContextId(q.contextid());
			m.setIncludeSubcategories(false);
			m.setActive(true);
			m.setLastSynced(LocalDateTime.now());
			changed = true;
		} else {
			if (!skillName.equalsIgnoreCase(m.getSkillName())) {
				m.setSkillName(skillName);
				changed = true;
			}
			if (!Objects.equals(m.getQcatId(), q.id())) {
				m.setQcatId(q.id());
				changed = true;
			}
			if (!Objects.equals(m.getHostCourseId(), hostCourseId)) {
				m.setHostCourseId(hostCourseId);
				changed = true;
			}
			if (!Objects.equals(m.getContextId(), q.contextid())) {
				m.setContextId(q.contextid());
				changed = true;
			}
			if (!Boolean.FALSE.equals(m.getIncludeSubcategories())) {
				m.setIncludeSubcategories(false);
				changed = true;
			}

			if (changed) {
				m.setLastSynced(LocalDateTime.now());
			}
		}

		if (changed) {
			mapRepo.save(m);
			if (isNew) {
				log.info("CREATED mapping '{}' -> qcat {}", skillName, q.id());
			} else {
				log.info("UPDATED mapping '{}' -> qcat {}", skillName, q.id());
			}
		} else {
			log.info("NO CHANGE for '{}', skipping update", skillName);
		}
	}

	private Optional<QCatInfo> matchCategoryForSkill(String skillName, List<QCatInfo> qcats) {
		if (skillName == null)
			return Optional.empty();
		String s = skillName.trim();
		if (s.isEmpty())
			return Optional.empty();

		for (QCatInfo q : qcats) {
			if (q.name() != null && q.name().equalsIgnoreCase(s))
				return Optional.of(q);
		}

		String normSkill = normalize(s);

		for (QCatInfo q : qcats) {
			String qn = normalize(q.name());
			if (qn.contains(normSkill))
				return Optional.of(q);
			if (normSkill.contains(qn) && qn.length() > 2)
				return Optional.of(q);
		}

		Set<String> tokens = Arrays.stream(normSkill.split("\\s+")).filter(t -> t.length() > 1)
				.collect(Collectors.toSet());
		if (!tokens.isEmpty()) {
			for (QCatInfo q : qcats) {
				String qn = normalize(q.name());
				Set<String> qtokens = Arrays.stream(qn.split("\\s+")).filter(t -> t.length() > 1)
						.collect(Collectors.toSet());
				if (qtokens.containsAll(tokens))
					return Optional.of(q);
			}
		}

		return Optional.empty();
	}

	private String normalize(String s) {
		if (s == null)
			return "";
		return s.replaceAll("[^A-Za-z0-9 ]", " ").replaceAll("\\s+", " ").trim().toLowerCase();
	}
}
