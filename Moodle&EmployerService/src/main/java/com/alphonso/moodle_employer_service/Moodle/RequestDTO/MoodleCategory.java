package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class MoodleCategory {
    private Long categoryId;
    private String name;
    private Long contextId; 
}
