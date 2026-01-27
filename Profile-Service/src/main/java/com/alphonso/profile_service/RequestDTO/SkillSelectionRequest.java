package com.alphonso.profile_service.RequestDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class SkillSelectionRequest {

    @NotNull(message = "Skill group ID is required")
    private Long groupId;

    @NotNull(message = "Skill role ID is required")
    private Long roleId;

    @NotEmpty(message = "Core skills cannot be empty")
    @Size(min = 10, max = 10, message = "You must select exactly 10 core skills")
    private List<Long> coreSkillIds;

    @Size(min = 5, max = 5, message = "You must select exactly 5 additional skills")
    private List<Long> additionalSkillIds;
    
    @NotNull(message = "Programming Id is required")
    private Long programmingId;
}