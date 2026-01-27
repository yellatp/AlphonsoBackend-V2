package com.alphonso.Interviewer_Service.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;
import com.alphonso.Interviewer_Service.Entity.SkillSetDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerDetailsResponse {
    private Long id;
    private String profileId;
    private String email;
    private String name;
    private Set<SkillSetDetails> skills;
    private LocalDateTime createdAt;
}
