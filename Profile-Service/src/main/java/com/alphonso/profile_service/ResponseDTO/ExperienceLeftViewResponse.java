package com.alphonso.profile_service.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
