package com.alphonso.moodle_employer_service.Moodle.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "moodle_course_mapping", uniqueConstraints = @UniqueConstraint(columnNames = { "moodle_course_id",
		"cohort_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodleCourseMappingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "moodle_course_id", nullable = false)
	private Long moodleCourseId;

	@Column(name = "skill_name", nullable = false)
	private String skillName;

	@Column(name = "moodle_course_name")
	private String moodleCourseName;

	@Column(name = "cohort_id", nullable = false)
	private Long cohortId;

	@Column(name = "cohort_idnumber")
	private String cohortIdnumber;

	@Column(name = "active")
	private Boolean active = true;

	private boolean processed = false;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "last_synced")
	private LocalDateTime lastSynced;
}
