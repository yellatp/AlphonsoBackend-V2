package com.alphonso.profile_service.RequestDTO;

import java.time.YearMonth;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobDetailRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth startDate;

    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth endDate; // Optional for currently working employees

    @NotNull(message = "Currently working status must be specified")
    private Boolean currentlyWorking;

    @Size(max = 500, message = "Role description can be up to 500 characters")
    private String roleDescription;
}