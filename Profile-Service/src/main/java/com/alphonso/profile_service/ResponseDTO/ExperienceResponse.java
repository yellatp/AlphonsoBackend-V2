package com.alphonso.profile_service.ResponseDTO;

import java.util.List;
import lombok.Data;

@Data
public class ExperienceResponse {
    private Boolean isExperienced;
    private List<JobDetailResponse> jobDetails;
}
