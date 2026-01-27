package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import lombok.Data;

@Data
public class CategoryDto {
    private Integer categoryid;
    private String categoryname;
    private Double earned;
    private Double possible;
    private Double percentage;
    private Integer questioncount;
}