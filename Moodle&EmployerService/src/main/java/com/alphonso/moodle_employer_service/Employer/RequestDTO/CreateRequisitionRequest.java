package com.alphonso.moodle_employer_service.Employer.RequestDTO;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRequisitionRequest {

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @NotNull(message = "Experience required is mandatory")
    @Min(value = 0, message = "Experience required must be 0 or more")
    private Integer experienceRequired;

    @NotBlank(message = "Employment type is required")   
    private String employmentType;         

    @NotBlank(message = "Location is required")
    private String location;                

    @NotNull(message = "Openings is required")
    @Min(value = 1, message = "Openings must be at least 1")
    private Integer openings;

    @NotBlank(message = "Job description is required")
    @Size(min = 10, max = 4000, message = "Job description must be between 10 and 4000 characters")
    private String jobDescription;

    @NotNull(message = "Required skill IDs are mandatory")
    @Size(min = 1, message = "At least one required skill must be selected")
    private Set<Long> requiredSkillIds;

    @Size(max = 50, message = "Nice-to-have skills cannot exceed 50 items")
    private Set<Long> niceSkillIds;

}
