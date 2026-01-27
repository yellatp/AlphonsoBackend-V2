package com.alphonso.moodle_employer_service.Moodle.ResponseDTO; 

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AssessmentResultResponse { 
	private String profileId;
	//@JsonProperty("OverAll Percentage")
	private Double moodlePercentage;
	private List<CategoryScoreResponse> categoryScores; 
}