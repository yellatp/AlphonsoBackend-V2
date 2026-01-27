package com.alphonso.moodle_employer_service.Employer.RequestDTO;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProfileDTO {

    private String profileId;
    private String name;
    private String email;
    private String role;
    private Integer totalExperience;
    private Set<String> coreSkills;
    private String status;
}