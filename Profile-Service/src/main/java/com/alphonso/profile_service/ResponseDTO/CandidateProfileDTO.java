package com.alphonso.profile_service.ResponseDTO;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CandidateProfileDTO {

    private String profileId;
    private String name;
    private String email;
    private String role;
    private Integer totalExperience;
    private Set<String> coreSkills;
    private String status;
}