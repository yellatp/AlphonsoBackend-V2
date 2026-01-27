package com.alphonso.moodle_employer_service.Moodle.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "v_assessment_attempt_category")
public class AssessmentAttemptCategoryView {

    @Id
    @Column(name = "category_score_id")
    private Long categoryScoreId;

    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "moodle_user_id")
    private Integer moodleUserId;

    @Column(name = "profile_id")
    private String profileId;

    @Column(name = "email")
    private String email;

    @Column(name = "moodle_quiz_id")
    private Integer quizId;

    @Column(name = "attempt_date")
    private LocalDateTime attemptDate;

    @Column(name = "score")
    private Double score;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "earned")
    private Double earned;

    @Column(name = "possible")
    private Double possible;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "question_count")
    private Integer questionCount;

}
