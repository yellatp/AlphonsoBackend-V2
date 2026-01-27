package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.CourseEntity;
import feign.Param;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

	boolean existsByMoodleCourseId(Long moodleCourseId);

	Optional<CourseEntity> findByMoodleCourseId(Long moodleCourseId);

	@Query("SELECT c FROM CourseEntity c WHERE LOWER(c.fullName) = LOWER(:skillName)")
	Optional<CourseEntity> findBySkillName(String skillName);

	@Query("SELECT c.moodleCourseId FROM CourseEntity c WHERE c.shortName = :shortName")
	Long findIdByShortName(@Param("shortName") String shortName);

	@Query("SELECT c FROM CourseEntity c WHERE c.categoryId IS NOT NULL AND c.visible = true")
	List<CourseEntity> findAllActiveCourses();

	@Query("SELECT c.moodleCourseId FROM CourseEntity c WHERE c.categoryId = :catId ORDER BY c.id ASC")
	Optional<Long> findFirstCourseByCategoryId(Long catId);

	@Query("select c.id from CourseEntity c where c.categoryId = :catId")
	List<Long> findFirstCourseIdByCategory(@Param("catId") Long catId, Pageable pageable);
}
