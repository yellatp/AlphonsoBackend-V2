package com.alphonso.moodle_employer_service.Moodle.OpenFeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.util.MultiValueMap;

@FeignClient(name = "moodle-api", url = "${moodle.base-url}")
public interface MoodleFeignClient {

	@GetMapping("/webservice/rest/server.php")
	Object getCategoriesByParent(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format, @RequestParam("criteria[0][key]") String criteriaKey,
			@RequestParam("criteria[0][value]") String criteriaValue);

	@GetMapping("/webservice/rest/server.php")
	Object getCoursesByField(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format, @RequestParam("field") String field,
			@RequestParam("value") String value);

	@GetMapping
	Object getAllCourses(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format);

	@GetMapping
	Object getCategories(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format);

	@GetMapping("/webservice/rest/server.php")
	Object getCategoryContext(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format, @RequestParam("categoryid") String categoryId);

	@PostMapping(value = "/webservice/rest/server.php", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	Object postForm(@RequestParam("wstoken") String token, @RequestParam("wsfunction") String function,
			@RequestParam("moodlewsrestformat") String format, @RequestBody MultiValueMap<String, String> form);
}