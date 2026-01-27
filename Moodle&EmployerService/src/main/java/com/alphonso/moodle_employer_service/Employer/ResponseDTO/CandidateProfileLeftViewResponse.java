package com.alphonso.moodle_employer_service.Employer.ResponseDTO;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CandidateProfileLeftViewResponse {

    private String profileId;

    private String fullName;
    private String role;
    private List<String> skills;

    private String positionType;
    private String location;

    private Boolean isExperienced;
    private List<ExperienceLeftViewResponse> experience;
}
