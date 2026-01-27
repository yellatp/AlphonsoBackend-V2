package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienceLeftViewResponse {

	private String companyName;
    private String jobTitle;
    private String startDate;
    private String endDate;
}
