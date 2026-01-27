package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionResponse {

	private String jobRole;

	private Integer experienceRequired;
	private String employmentType;
	private String location;

	private Integer openings;

	private String status;
	private String jobDescription;

	private Set<String> requiredSkills;

}
