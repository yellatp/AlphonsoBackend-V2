package com.alphonso.moodle_employer_service.Moodle.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Assessment_Attempt")

public class AssessmentAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "moodle_attempt_id", unique = true)
	private Long moodleAttemptId;

	@Column(name = "moodle_user_id")
	private Integer moodleUserId;

	@Column(name = "moodle_quiz_id")
	private Integer moodleQuizId;

	@Column(name = "attempt_date")
	private LocalDateTime attemptDate;

	private Double score;

	@Column(name = "email_id")
	private String emailId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moodle_profile_id")
	private MoodleProfile moodleProfile;

}