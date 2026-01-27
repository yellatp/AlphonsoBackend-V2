package com.alphonso.moodle_employer_service.Moodle.Service;

import org.springframework.stereotype.Service;
import com.alphonso.moodle_employer_service.Moodle.Config.MoodleConfig;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentAttempt;
import com.alphonso.moodle_employer_service.Moodle.Entity.AssessmentCategoryScore;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillQbankCategoryMap;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile.Status;
import com.alphonso.moodle_employer_service.Moodle.Exception.MoodleSyncException;
import com.alphonso.moodle_employer_service.Moodle.OpenFeign.MoodleFeignClient;
import com.alphonso.moodle_employer_service.Moodle.Repository.AssessmentAttemptRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.AssessmentCategoryScoreRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.CourseRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.MoodleProfileRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.ProfileSkillRepository;
import com.alphonso.moodle_employer_service.Moodle.Repository.SkillQbankCategoryMapRepository;
import com.alphonso.moodle_employer_service.Moodle.RequestDTO.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodleApiService {

	private final MoodleProfileRepository profileRepo;
	private final ProfileSkillRepository profileSkillRepo;
	private final MoodleConfig moodleConfig;
	private final MoodleFeignClient moodleFeignClient;
	private final CourseRepository courseRepo;
	private final SkillQbankCategoryMapRepository mapRepo;
	private final AssessmentAttemptRepository attemptRepo;
	private final AssessmentCategoryScoreRepository catRepo;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAllCourses() {
		try {
			return (List<Map<String, Object>>) moodleFeignClient.getAllCourses(moodleConfig.getToken(),
					"core_course_get_courses", moodleConfig.getFormat());
		} catch (Exception e) {
			throw new MoodleSyncException("Unable to get all courses from Moodle " + e.getMessage(), e);
		}
	}

	public List<Map<String, Object>> getAllCoursesFromSubcategories(Long mainCategoryId) {
		try {
			Object catsResp = moodleFeignClient.getCategoriesByParent(moodleConfig.getToken(),
					"core_course_get_categories", moodleConfig.getFormat(), "parent", String.valueOf(mainCategoryId));

			JsonNode cats = coerceToJson(catsResp);
			if (cats == null) {
				throw new MoodleSyncException("Null JSON from core_course_get_categories");
			}
			if (cats.has("exception") || cats.has("errorcode")) {
				throw new MoodleSyncException("WS error: " + cats.path("errorcode").asText() + " - "
						+ cats.path("exception").asText() + " - " + cats.path("message").asText());
			}
			if (!cats.isArray()) {
				throw new MoodleSyncException("Expected an array of categories; got: " + cats.toString());
			}

			List<Map<String, Object>> allCourses = new ArrayList<>();

			for (JsonNode subCat : cats) {
				long subCatId = subCat.path("id").asLong(0);
				if (subCatId <= 0)
					continue;

				Object crsResp = moodleFeignClient.getCoursesByField(moodleConfig.getToken(),
						"core_course_get_courses_by_field", moodleConfig.getFormat(), "category",

						String.valueOf(subCatId));

				JsonNode crsJson = coerceToJson(crsResp);
				if (crsJson == null) {
					log.warn("Null JSON from get_courses_by_field for category {}", subCatId);
					continue;
				}
				if (crsJson.has("exception") || crsJson.has("errorcode")) {
					throw new MoodleSyncException("WS error (courses): " + crsJson.path("errorcode").asText() + " - "
							+ crsJson.path("exception").asText() + " - " + crsJson.path("message").asText());
				}

				JsonNode coursesArr = crsJson.path("courses");
				if (coursesArr.isArray()) {
					for (JsonNode c : coursesArr) {
						Map<String, Object> m = new LinkedHashMap<>();
						m.put("id", c.path("id").asLong());
						m.put("fullname", c.path("fullname").asText(null));
						m.put("shortname", c.path("shortname").asText(null));
						m.put("summary", c.path("summary").asText(null));
						Long categoryId = c.has("categoryid") ? c.path("categoryid").asLong()
								: c.path("category").asLong();
						m.put("categoryid", categoryId);
						Integer visible = c.has("visible") ? c.path("visible").asInt() : 1;
						m.put("visible", visible);
						allCourses.add(m);
					}
				}
			}

			return allCourses;

		} catch (Exception e) {
			throw new MoodleSyncException("Failed to fetch courses from subcategories of " + mainCategoryId, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAllCategories() {
		try {
			return (List<Map<String, Object>>) moodleFeignClient.getCategories(moodleConfig.getToken(),
					"core_course_get_categories", moodleConfig.getFormat());
		} catch (Exception e) {
			throw new MoodleSyncException("Unable to fetch categories: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> createCohort(String name, String idNumber, String description,
			long courseCategoryId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("cohorts[0][name]", name);
			form.add("cohorts[0][idnumber]", idNumber);
			form.add("cohorts[0][description]", description);
			form.add("cohorts[0][categorytype][type]", "id");
			form.add("cohorts[0][categorytype][value]", String.valueOf(courseCategoryId));

			Object response = moodleFeignClient.postForm(moodleConfig.getToken(), "core_cohort_create_cohorts",
					moodleConfig.getFormat(), form);

			if (response instanceof List<?> list) {
				return (List<Map<String, Object>>) list;
			}

			if (response instanceof Map<?, ?> map) {
				String message = String.valueOf(map.get("message"));
				String exception = String.valueOf(map.get("exception"));

				if (message != null && message.toLowerCase().contains("already exists")) {
					log.warn("Cohort '{}' already exists, fetching existing by idnumber {}", name, idNumber);
					return getCohortByIdNumber(idNumber);
				}

				if (exception != null && exception.contains("invalid_parameter")) {
					log.info("Retrying cohort creation with categorytype[idnumber] for category {}", courseCategoryId);

					String catIdNumber = getCategoryIdNumber(courseCategoryId);

					MultiValueMap<String, String> form2 = new LinkedMultiValueMap<>();
					form2.add("cohorts[0][name]", name);
					form2.add("cohorts[0][idnumber]", idNumber);
					form2.add("cohorts[0][description]", description);
					form2.add("cohorts[0][categorytype][type]", "idnumber");
					form2.add("cohorts[0][categorytype][value]", catIdNumber);

					Object resp2 = moodleFeignClient.postForm(moodleConfig.getToken(), "core_cohort_create_cohorts",
							moodleConfig.getFormat(), form2);

					if (resp2 instanceof List<?> ok) {
						return (List<Map<String, Object>>) ok;
					}
					if (resp2 instanceof Map<?, ?> m2) {
						String msg2 = String.valueOf(m2.get("message"));
						String ex2 = String.valueOf(m2.get("exception"));
						if (msg2 != null && msg2.toLowerCase().contains("already exists")) {
							return getCohortByIdNumber(idNumber);
						}
						throw new MoodleSyncException(
								"Moodle cohort creation failed (idnumber mode): " + ex2 + " - " + msg2);
					}

					throw new MoodleSyncException("Unexpected response (idnumber mode): " + safeToString(resp2));
				}

				throw new MoodleSyncException("Moodle cohort creation failed: " + exception + " - " + message);
			}

			throw new MoodleSyncException("Unexpected response type while creating cohort: " + safeToString(response));

		} catch (Exception e) {
			throw new MoodleSyncException("Unable to create cohort: " + name, e);
		}
	}

	private String getCategoryIdNumber(long categoryId) {
		Object resp = moodleFeignClient.getCategoriesByParent(moodleConfig.getToken(), "core_course_get_categories",
				moodleConfig.getFormat(), "ids", String.valueOf(categoryId));
		JsonNode j = coerceToJson(resp);
		if (j != null && j.isArray() && j.size() > 0) {
			return j.get(0).path("idnumber").asText("");
		}
		throw new MoodleSyncException(
				"Could not resolve idnumber for category " + categoryId + " payload=" + safeToString(resp));
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getCohortByIdNumber(String idNumber) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("idnumber", idNumber);

			Object response = moodleFeignClient.postForm(moodleConfig.getToken(), "core_cohort_get_cohorts",
					moodleConfig.getFormat(), form);

			if (response instanceof List<?> list) {
				return (List<Map<String, Object>>) list;
			}

			throw new MoodleSyncException("Invalid response while fetching existing cohort");

		} catch (Exception e) {
			throw new MoodleSyncException("Unable to fetch cohort by idnumber: " + idNumber, e);
		}
	}

	public Object linkCohortToCourse(Long courseId, Long cohortId, Long roleId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("courseid", String.valueOf(courseId));
			form.add("cohortid", String.valueOf(cohortId));
			form.add("roleid", String.valueOf(roleId));

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "local_moodleapi_add_instance",
					moodleConfig.getFormat(), form);

			log.info("Linked cohort {} to course {}", cohortId, courseId);
			return resp;

		} catch (Exception e) {
			throw new MoodleSyncException("Unable to link cohort " + cohortId + " with course " + courseId, e);
		}
	}

	@SuppressWarnings("unchecked")
	public Long createOrGetUser(String firstName, String lastName, String email, boolean forcePwdChange) {

		Long existingByEmail = findUserIdByField("email", email);
		if (existingByEmail != null)
			return existingByEmail;

		String username = firstName.toLowerCase() + UUID.randomUUID().toString().substring(0, 4);

		System.out.println(username);

		Long existingByUsername = findUserIdByField("username", username);
		if (existingByUsername != null)
			return existingByUsername;

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("users[0][username]", username);
		form.add("users[0][password]", "Welcome@123");
		form.add("users[0][firstname]", firstName);
		form.add("users[0][lastname]", lastName);
		form.add("users[0][email]", email);

		Object response = moodleFeignClient.postForm(moodleConfig.getToken(), "core_user_create_users",
				moodleConfig.getFormat(), form);

		if (response instanceof List<?> list && !list.isEmpty()) {
			Map<String, Object> userMap = (Map<String, Object>) list.get(0);
			return ((Number) userMap.get("id")).longValue();
		}

		if (response instanceof Map<?, ?> map) {
			String exception = String.valueOf(map.get("exception"));
			String message = String.valueOf(map.get("message"));
			String debuginfo = String.valueOf(map.get("debuginfo"));
			throw new MoodleSyncException("Moodle error creating user: " + exception + " - " + message
					+ (debuginfo != null ? " (debug: " + debuginfo + ")" : ""));
		}

		throw new MoodleSyncException("Unknown Moodle response creating user: " + response);
	}

	@SuppressWarnings("unchecked")
	private Long findUserIdByField(String field, String value) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("field", field);
			form.add("values[0]", value);

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "core_user_get_users_by_field",
					moodleConfig.getFormat(), form);

			if (resp instanceof List<?> list && !list.isEmpty()) {
				Map<String, Object> first = (Map<String, Object>) list.get(0);
				return ((Number) first.get("id")).longValue();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean addUserToCohort(Long userId, Long cohortId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("members[0][cohorttype][type]", "id");
			form.add("members[0][cohorttype][value]", String.valueOf(cohortId));
			form.add("members[0][usertype][type]", "id");
			form.add("members[0][usertype][value]", String.valueOf(userId));

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "core_cohort_add_cohort_members",
					moodleConfig.getFormat(), form);

			if (resp instanceof Map<?, ?> map) {
				if (map.containsKey("exception")) {
					String code = String.valueOf(map.get("errorcode"));
					String msg = String.valueOf(map.get("message"));
					if ((code != null && code.toLowerCase().contains("memberexists"))
							|| (msg != null && msg.toLowerCase().contains("already"))) {
						log.info("User {} already in cohort {}", userId, cohortId);
						return true;
					}
					throw new MoodleSyncException("Moodle error (add cohort member): " + code + " - " + msg);
				}
			}

			log.info("Added user {} to cohort {}", userId, cohortId);
			return true;

		} catch (Exception e) {
			log.error("Failed to add user {} to cohort {}: {}", userId, cohortId, e.getMessage(), e);
			return false;
		}
	}

	public boolean addUsersToCohortBulk(List<Long> userIds, Long cohortId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			for (int i = 0; i < userIds.size(); i++) {
				form.add("members[" + i + "][cohorttype][type]", "id");
				form.add("members[" + i + "][cohorttype][value]", String.valueOf(cohortId));
				form.add("members[" + i + "][usertype][type]", "id");
				form.add("members[" + i + "][usertype][value]", String.valueOf(userIds.get(i)));
			}

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "core_cohort_add_cohort_members",
					moodleConfig.getFormat(), form);

			if (resp instanceof Map<?, ?> map && map.containsKey("exception")) {
				String code = String.valueOf(map.get("errorcode"));
				String msg = String.valueOf(map.get("message"));
				throw new MoodleSyncException("Moodle error (bulk add): " + code + " - " + msg);
			}

			log.info("Added {} users to cohort {}", userIds.size(), cohortId);
			return true;

		} catch (Exception e) {
			log.error("Bulk add users to cohort {} failed: {}", cohortId, e.getMessage(), e);
			return false;
		}
	}

	public ContextInfo getContextForCourse(Long courseId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("courseid", String.valueOf(courseId));

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "local_moodleapi_get_course_context",
					moodleConfig.getFormat(), form);

			JsonNode root = coerceToJson(resp);
			if (root == null) {
				throw new RuntimeException("Null response when fetching course context for courseId=" + courseId);
			}
			if (root.has("exception") || root.has("errorcode")) {
				throw new RuntimeException("Moodle error getting course context: " + root.toString());
			}

			long ctxid = root.path("contextid").asLong(0);
			int ctxlevel = root.path("contextlevel").asInt(0);
			long instanceid = root.path("instanceid").asLong(0);

			if (ctxid <= 0) {
				throw new RuntimeException(
						"No valid contextid returned for course " + courseId + ". Raw: " + root.toString());
			}
			return new ContextInfo(ctxid, ctxlevel, instanceid);

		} catch (Exception e) {
			String msg = "Failed to get context for course " + courseId + ": " + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public List<QCatInfo> getQbankCategories(Long hostCourseId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("courseid", String.valueOf(hostCourseId));

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "local_wsquiz_get_qcats_for_course",
					moodleConfig.getFormat(), form);

			JsonNode root = coerceToJson(resp);
			if (root == null) {
				throw new RuntimeException("Null JSON returned from getQbankCategories");
			}
			if (root.has("exception") || root.has("errorcode")) {
				throw new RuntimeException("Moodle error getting qcats: " + root.toString());
			}

			List<QCatInfo> out = new ArrayList<>();
			if (root.isArray()) {
				out = objectMapper.convertValue(root, new TypeReference<List<QCatInfo>>() {
				});
			} else if (root.has("qcats") && root.path("qcats").isArray()) {
				out = objectMapper.convertValue(root.path("qcats"), new TypeReference<List<QCatInfo>>() {
				});
			} else {
				try {
					QCatInfo single = objectMapper.treeToValue(root, QCatInfo.class);
					if (single != null)
						out.add(single);
				} catch (Exception ignored) {
				}
			}

			return out;
		} catch (Exception e) {
			String msg = "Failed to fetch qbank categories for hostCourseId=" + hostCourseId + ": " + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public String launchUserQuiz(String email) {

		String hostCourseShortname = "AssessmentHost";

		MoodleProfile user = profileRepo.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Profile not found for " + email));

		List<SkillEntity> skillNames = profileSkillRepo.findSkillsByUserId(user.getId());
		if (skillNames == null || skillNames.isEmpty()) {
			throw new RuntimeException("User has no mapped skills");
		}

		List<String> skills = skillNames.stream().map(SkillEntity::getSkillName).collect(Collectors.toList());

		Long hostCourseId = courseRepo.findIdByShortName(hostCourseShortname);
		if (hostCourseId == null) {
			throw new RuntimeException("Invalid host course shortname: " + hostCourseShortname);
		}

		ContextInfo ctx = getContextForCourse(hostCourseId);
		long courseContextId = ctx.id();

		List<SkillQbankCategoryMap> maps = mapRepo.findBySkillNameIgnoreCase(skills);
		if (maps.isEmpty()) {
			throw new RuntimeException("No qbank mappings found for skills=" + skills);
		}

		List<Long> qcatIds = maps.stream().map(SkillQbankCategoryMap::getQcatId).toList();

		QuizModuleIds ids = createQuizModuleAndGetIds(hostCourseId, "Skill Assessment", "");

		Long quizId = ids.getQuizId();
		Long cmid = ids.getCmid();

		if (cmid == null || cmid <= 0) {
			cmid = resolveCmidByInstance(hostCourseId, quizId);
		}

		int totalQuestions = 100;
		int n = qcatIds.size();
		int base = totalQuestions / n;
		int rem = totalQuestions % n;

		for (int i = 0; i < n; i++) {
			long qcatId = qcatIds.get(i);
			int take = base + (i < rem ? 1 : 0);
			if (take <= 0)
				continue;

			try {
				addRandomQuestionsByQcats(cmid, quizId, qcatId, take);
			} catch (Exception ex) {

				addRandomQuestionsWithFilter(cmid, quizId, courseContextId, qcatId, take);
			}
		}

		return generateMoodleQuizLoginWithCmid(email, cmid);
	}

	private QuizModuleIds createQuizModuleAndGetIds(Long courseId, String quizName, String introHtml) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("courseid", String.valueOf(courseId));
			form.add("name", quizName);
			form.add("intro", introHtml == null ? "" : introHtml);

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "local_wsquiz_create_quiz_module",
					moodleConfig.getFormat(), form);

			String raw = safeToString(resp);
			JsonNode json = coerceToJson(resp);
			if (json == null) {
				throw new IllegalStateException("Null JSON from " + "local_wsquiz_create_quiz_module" + ". Raw=" + raw);
			}
			if (json.has("exception") || json.has("errorcode")) {
				throw new RuntimeException("Moodle error in create_quiz_module: " + json.toString());
			}

			QuizModuleIds ids = new QuizModuleIds();
			ids.setCourseId(json.path("courseid").isNumber() ? json.get("courseid").asLong() : courseId);
			ids.setQuizId(json.path("quizid").isNumber() ? json.get("quizid").asLong() : null);
			ids.setCmid(json.path("cmid").isNumber() ? json.get("cmid").asLong() : null);

			if (ids.getQuizId() == null || ids.getCmid() == null) {
				throw new IllegalStateException("create_quiz_module did not return quizid/cmid. Payload: " + raw);
			}
			return ids;
		} catch (Exception e) {
			String msg = "Failed to create quiz module for course " + courseId + ": " + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private Long resolveCmidByInstance(Long courseId, Long quizId) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("courseid", String.valueOf(courseId));
			form.add("module", "quiz");
			form.add("instance", String.valueOf(quizId));

			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(),
					"core_course_get_course_module_by_instance", moodleConfig.getFormat(), form);

			JsonNode json = coerceToJson(resp);
			if (json == null || json.has("exception") || json.has("errorcode")) {
				throw new RuntimeException("Moodle error in get_course_module_by_instance: " + safeToString(resp));
			}

			long cmid = json.path("cm").path("id").asLong(0);
			if (cmid <= 0)
				cmid = json.path("id").asLong(0);
			if (cmid <= 0)
				throw new RuntimeException("Could not resolve cmid for quiz " + quizId);
			return cmid;
		} catch (Exception e) {
			String msg = "Failed to resolve cmid for quiz " + quizId + " in course " + courseId + ": " + e.getMessage();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void addRandomQuestionsWithFilter(Long cmid, Long quizId, long contextId, long qcatId, int count) {

		String filterJson = String.format("{\"category\":\"%d,%d\"}", contextId, qcatId);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("cmid", String.valueOf(cmid));
		form.add("addonpage", "-1");
		form.add("randomcount", String.valueOf(count));
		form.add("filtercondition", filterJson);

		Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "mod_quiz_add_random_questions",
				moodleConfig.getFormat(), form);

		JsonNode json = coerceToJson(resp);
		if (json == null) {
			throw new IllegalStateException("Null JSON adding random questions (cmid=" + cmid + ", quizId=" + quizId
					+ ", qcatId=" + qcatId + ")");
		}
		if (json.has("exception") || json.has("errorcode")) {
			throw new RuntimeException("Error adding random: " + json.toString() + " (filter=" + filterJson + ")");
		}
		log.info("Added {} random questions (qcat={}, ctx={}, cmid={}, quiz={})", count, qcatId, contextId, cmid,
				quizId);
	}

	private List<Map<String, Object>> addRandomQuestionsByQcats(Long cmid, Long quizId, long qcatId, int count) {
		try {
			List<Map<String, Object>> qcats = new ArrayList<>();
			Map<String, Object> e = new HashMap<>();
			e.put("qcatid", (int) qcatId);
			e.put("count", count);
			qcats.add(e);

			String qcatsJson = objectMapper.writeValueAsString(qcats);

			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("cmid", String.valueOf(cmid));
			form.add("qcatsjson", qcatsJson);
			Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "local_wsquiz_add_random_by_qcats",
					moodleConfig.getFormat(), form);
			JsonNode root = coerceToJson(resp);
			if (root == null)
				throw new RuntimeException("Null JSON response from " + "local_wsquiz_add_random_by_qcats");

			if (root.has("exception") || root.has("errorcode")) {
				throw new RuntimeException("Moodle error " + root.toString());
			}

			return objectMapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {
			});
		} catch (Exception e) {
			String msg = "Failed addRandomQuestionsByQcats for qcat " + qcatId + ": " + e.getMessage();
			log.warn(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

//	@SuppressWarnings("unchecked")
//	private String generateMoodleQuizLoginWithCmid(String email, Long cmid) {
//		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
//		form.add("user[email]", email);
//
//		Object resp = moodleFeignClient.postForm(moodleConfig.getToken(), "auth_userkey_request_login_url",
//				moodleConfig.getFormat(), form);
//
//		String raw = safeToString(resp);
//		log.info("auth_userkey_request_login_url raw: {}", raw);
//
//		Map<String, Object> map;
//		if (resp instanceof Map<?, ?>) {
//			map = (Map<String, Object>) resp;
//		} else {
//			try {
//				map = objectMapper.readValue(raw, new TypeReference<>() {
//				});
//			} catch (Exception e) {
//				throw new RuntimeException("invalid payload: " + raw, e);
//			}
//		}
//
//		String loginUrl = (String) map.get("loginurl");
//		if (loginUrl == null)
//			throw new RuntimeException("no loginurl in payload: " + raw);
//
//		String base = moodleConfig.getBaseUrl();
//		if (base == null)
//			base = "";
//		base = base.replaceAll("/+(webservice.*)?$", "").replaceAll("/+$", "");
//		String wants = base + "/mod/quiz/view.php?id=" + cmid;
//		String full = loginUrl + "&wantsurl=" + URLEncoder.encode(wants, StandardCharsets.UTF_8);
//
//		log.info("auth link: {}, wants: {}", loginUrl, wants);
//		return full;
//	}

	@SuppressWarnings("unchecked")
	private String generateMoodleQuizLoginWithCmid(String email, Long cmid) {

		 Optional<MoodleProfile> profileOpt = Optional.empty();

	    try {
	        // ✅ Fetch profile initially (optional but useful)
	    	profileOpt = profileRepo.findByEmail(email);

	        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
	        form.add("user[email]", email);

	        Object resp = moodleFeignClient.postForm(
	                moodleConfig.getToken(),
	                "auth_userkey_request_login_url",
	                moodleConfig.getFormat(),
	                form
	        );

	        String raw = safeToString(resp);
	        log.info("auth_userkey_request_login_url raw: {}", raw);

	        Map<String, Object> map;
	        if (resp instanceof Map<?, ?>) {
	            map = (Map<String, Object>) resp;
	        } else {
	            try {
	                map = objectMapper.readValue(raw, new TypeReference<>() {});
	            } catch (Exception e) {
	                throw new RuntimeException("invalid payload: " + raw, e);
	            }
	        }

	        String loginUrl = (String) map.get("loginurl");
	        if (loginUrl == null) {
	            throw new RuntimeException("no loginurl in payload: " + raw);
	        }

	        String base = moodleConfig.getBaseUrl();
	        if (base == null) base = "";
	        base = base.replaceAll("/+(webservice.*)?$", "").replaceAll("/+$", "");

	        String wants = base + "/mod/quiz/view.php?id=" + cmid;
	        String fullLink = loginUrl + "&wantsurl=" + URLEncoder.encode(wants, StandardCharsets.UTF_8);

	        log.info("auth link: {}, wants: {}", loginUrl, wants);

	        profileOpt.ifPresent(profile -> {
	            profile.setStatus(Status.QUIZ_COMPLETED);
	            profileRepo.save(profile);
	        });

	        return fullLink;

	    } catch (Exception ex) {
	        try {
	          
	        	 profileOpt.ifPresent(profile -> {
	                 profile.setStatus(Status.INPROGRESS);
	                 profileRepo.save(profile);
	             });
	        } catch (Exception ignored) {
	            
	        }

	        throw ex;
	    }
	}

	public void syncUserAttemptsUsingFeign(Long moodleUserId, Long sinceEpochSeconds) {

	    MoodleProfile moodleProfile = profileRepo.findByMoodleUserId(moodleUserId)
	            .orElseThrow(() -> new RuntimeException("Profile not found for " + moodleUserId));

	    try {
	        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
	        form.add("moodleuserid", String.valueOf(moodleUserId));
	        form.add("since", String.valueOf(sinceEpochSeconds));

	        Object resp = moodleFeignClient.postForm(
	                moodleConfig.getToken(),
	                "local_wsquiz_get_user_attempt_results",
	                moodleConfig.getFormat(),
	                form
	        );

	        JsonNode root = coerceToJson(resp);
	        if (root == null)
	            throw new RuntimeException("Null JSON response from WS");

	        if (root.has("exception") || root.has("errorcode")) {
	            throw new RuntimeException("Moodle error: " + root.toString());
	        }

	        List<AttemptResponse> attempts = objectMapper.convertValue(
	                root,
	                new TypeReference<List<AttemptResponse>>() {}
	        );

	        for (AttemptResponse ar : attempts) {

	            Long moodleAttemptId = ar.getAttemptid();
	            if (moodleAttemptId == null)
	                continue;

	            Optional<AssessmentAttempt> existing = attemptRepo.findByMoodleAttemptId(moodleAttemptId);
	            if (existing.isPresent()) {
	                continue;
	            }

	            AssessmentAttempt at = new AssessmentAttempt();
	            at.setMoodleAttemptId(moodleAttemptId);
	            at.setMoodleUserId(ar.getUserid());
	            at.setMoodleQuizId(ar.getQuizid());
	            long finish = ar.getTimefinish() != null ? ar.getTimefinish() : ar.getTimestart();
	            at.setAttemptDate(Instant.ofEpochSecond(finish)
	                    .atZone(ZoneId.systemDefault())
	                    .toLocalDateTime());

	            at.setScore(ar.getTotalearned() != null ? ar.getTotalearned() : ar.getPercentage());

	            if (profileRepo != null) {
	                Long moodleUid = ar.getUserid() != null ? ar.getUserid().longValue() : null;

	                if (moodleUid != null) {
	                    profileRepo.findByMoodleUserId(moodleUid).ifPresent(profile -> {
	                        at.setEmailId(profile.getEmail());
	                        at.setMoodleProfile(profile);
	                    });
	                }
	            }

	            AssessmentAttempt saved = attemptRepo.save(at);

	            if (ar.getCategories() != null) {
	                for (CategoryDto c : ar.getCategories()) {
	                    AssessmentCategoryScore cs = new AssessmentCategoryScore();
	                    cs.setAttemptRefId(saved.getId());
	                    cs.setCategoryId(c.getCategoryid());
	                    cs.setCategoryName(c.getCategoryname());
	                    cs.setEarned(c.getEarned());
	                    cs.setPossible(c.getPossible());
	                    cs.setPercentage(c.getPercentage());
	                    cs.setQuestionCount(c.getQuestioncount());
	                    catRepo.save(cs);
	                }
	            }
	        }

	        Double score = attemptRepo.findScoresByMoodleUserId(moodleProfile.getMoodleUserId().intValue());

	        if (score == null) {
	            log.warn("No score found for moodleUserId={}, skipping pass/fail calculation", moodleUserId);
	            return;   
	        }

	        double totalMarks = 100.0;
	        double percentage = (score / totalMarks) * 100;
	        percentage = Math.round(percentage * 100.0) / 100.0;

	        if (percentage >= 75.0) {
	            moodleProfile.setStatus(Status.QUIZ_PASSED);
	        } else {
	            moodleProfile.setStatus(Status.QUIZ_FAILED);
	        }

	        profileRepo.save(moodleProfile);

	    } catch (Exception e) {
	        log.warn("syncUserAttemptsUsingFeign failed for moodleUserId {} : {}", moodleUserId, e.getMessage(), e);
	        throw new RuntimeException(e);
	    }
	}


	public ProfileResultResponse syncProfileResultsProfiles(String profileId) {

		MoodleProfile moodleProfile = profileRepo.findByProfileId(profileId)
				.orElseThrow(() -> new RuntimeException("Profile not found for " + profileId));

		ProfileResultResponse result = new ProfileResultResponse();
		result.setProfileId(profileId);
		result.setStatus(moodleProfile.getStatus().toString());

		return result;
	}

	private JsonNode coerceToJson(Object resp) {
		try {
			if (resp == null)
				return null;
			if (resp instanceof String s)
				return objectMapper.readTree(s);
			return objectMapper.valueToTree(resp);
		} catch (Exception e) {
			log.warn("Failed to parse response as JSON: {}", e.getMessage());
			return null;
		}
	}

	private String safeToString(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			return String.valueOf(obj);
		}
	}

	@Data
	private static class QuizModuleIds {
		private Long courseId;
		private Long quizId;
		private Long cmid;
	}

}