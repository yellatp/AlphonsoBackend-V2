package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import java.util.List;
import lombok.Data;

@Data
public class AttemptResponse {
    private Long attemptid;
    private Integer quizid;
    private Integer userid;
    private Long timestart;
    private Long timefinish;
    private Double totalearned;
    private Double totalpossible;
    private Double percentage;
    private List<CategoryDto> categories;
}