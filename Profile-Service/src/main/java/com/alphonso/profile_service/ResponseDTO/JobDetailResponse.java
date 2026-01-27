package com.alphonso.profile_service.ResponseDTO;

import java.time.YearMonth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class JobDetailResponse {
    private String companyName;
    private String jobTitle;
    private String location;
	@JsonFormat(pattern = "yyyy-MM")
	private YearMonth startDate;
	@JsonFormat(pattern = "yyyy-MM")
	private YearMonth endDate;
    private Boolean currentlyWorking;
    private String roleDescription;
}
