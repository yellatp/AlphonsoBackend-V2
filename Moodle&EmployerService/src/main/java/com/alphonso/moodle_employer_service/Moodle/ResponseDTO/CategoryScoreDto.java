package com.alphonso.moodle_employer_service.Moodle.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryScoreDto {
    private Integer categoryId;
    private String categoryName;
    private Double earned;
    private Double possible;
    private Double percentage;
    private Integer questionCount;

}
