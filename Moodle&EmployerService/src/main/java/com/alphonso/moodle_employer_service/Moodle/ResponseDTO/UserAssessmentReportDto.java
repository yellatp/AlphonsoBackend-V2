package com.alphonso.moodle_employer_service.Moodle.ResponseDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAssessmentReportDto {
    private String profileId;
    private String email;
    private Integer moodleUserId;
    private Double totalScore;
    private List<AttemptDto> attempts;

}
