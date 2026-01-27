package com.alphonso.moodle_employer_service.Moodle.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor 
@Builder
public class CategoryScoreResponse {

	private String categoryName; 
	private Double percentage;
}
