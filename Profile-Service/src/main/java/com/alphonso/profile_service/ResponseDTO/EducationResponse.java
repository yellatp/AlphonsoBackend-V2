package com.alphonso.profile_service.ResponseDTO;

import java.time.LocalDate;
import lombok.Data;

@Data
public class EducationResponse {

    private String educationLevel;

    private String collegeName;

    private String areaOfStudy;

    private LocalDate startDate;

    private LocalDate endDate;
}
