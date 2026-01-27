package com.alphonso.moodle_employer_service.Moodle.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
public class CourseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "moodle_course_id", nullable = false, unique = true)
	private Long moodleCourseId;

	private String shortName;
	private String fullName;

	@Column(columnDefinition = "TEXT")
	private String summary;

	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "visible")
	private Boolean visible = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "last_synced")
	private LocalDateTime lastSynced;
}
