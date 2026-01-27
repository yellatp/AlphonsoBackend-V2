package com.alphonso.moodle_employer_service.Moodle.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skill_qbank_category_map", indexes = { @Index(name = "idx_skill_upper", columnList = "skill_name") })
public class SkillQbankCategoryMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "skill_name", nullable = false)
	private String skillName;

	@Column(name = "qcat_id", nullable = false)
	private Long qcatId;

	@Column(name = "host_course_id", nullable = false)
	private Long hostCourseId;

	@Column(name = "context_id", nullable = false)
	private Long contextId;

	@Column(name = "include_subcategories")
	private Boolean includeSubcategories = false;

	@Column(name = "active")
	private Boolean active = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "last_synced")
	private LocalDateTime lastSynced;

}