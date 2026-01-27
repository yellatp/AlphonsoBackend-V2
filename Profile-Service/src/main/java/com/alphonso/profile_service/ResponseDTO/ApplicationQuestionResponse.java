package com.alphonso.profile_service.ResponseDTO;

import com.alphonso.profile_service.Entity.ApplicationQuestions.JobPreference;
import lombok.Data;

@Data
public class ApplicationQuestionResponse {

    private Boolean legalAgeToWork;

    private JobPreference jobPreference;

    private Boolean willingToRelocate;

    private Boolean requireSponsorship;
}
