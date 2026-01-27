package com.alphonso.profile_service.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammingSkillResponse {
    private Long id;
    private String name; // Maps from programName
}
