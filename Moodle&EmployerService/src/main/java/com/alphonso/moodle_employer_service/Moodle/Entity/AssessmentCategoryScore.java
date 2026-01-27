package com.alphonso.moodle_employer_service.Moodle.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "Assessment_Category_Score")
public class AssessmentCategoryScore {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private Long attemptRefId;
    private Integer categoryId;
    private String categoryName;
    private Double earned;
    private Double possible;
    private Double percentage;
    private Integer questionCount;
}