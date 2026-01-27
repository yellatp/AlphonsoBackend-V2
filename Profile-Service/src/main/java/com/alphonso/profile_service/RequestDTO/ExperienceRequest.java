package com.alphonso.profile_service.RequestDTO;

import lombok.Data;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Data
public class ExperienceRequest {

    private Boolean isExperienced;

    @Size(max = 10, message = "You can provide up to 10 job details")
    private List<@Valid JobDetailRequest> jobDetails;
}