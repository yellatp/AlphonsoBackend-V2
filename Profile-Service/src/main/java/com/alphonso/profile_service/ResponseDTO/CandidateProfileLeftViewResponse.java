package com.alphonso.profile_service.ResponseDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
