package com.alphonso.profile_service.RequestDTO;

import com.alphonso.profile_service.Entity.ApplicationQuestions.JobPreference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationQuestionRequest {

    @NotNull(message = "Please specify if you are of legal age to work")
    @JsonProperty("Are you of legal age to work in the country")
    private Boolean legalAgeToWork;

    @NotNull(message = "Job preference must be provided")
    @JsonProperty("Job Preference")
    private JobPreference jobPreference;

    @NotNull(message = "Please specify if you are willing to relocate")
    @JsonProperty("Willing to Relocate")
    private Boolean willingToRelocate;

    @NotNull(message = "Please specify if you require sponsorship")
    @JsonProperty("Will you now or in the future require sponsorship")
    private Boolean requireSponsorship;
}
