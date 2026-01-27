package com.alphonso.moodle_employer_service.Moodle.ResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttemptDto {
    private Long attemptId;
    private Integer quizId;
    private LocalDateTime attemptDate;
    private Double score;
    private List<CategoryScoreDto> categories;

}
