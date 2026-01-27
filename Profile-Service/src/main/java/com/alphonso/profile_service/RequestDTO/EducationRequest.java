package com.alphonso.profile_service.RequestDTO;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class EducationRequest {

    @NotBlank(message = "Education level is required")
    private String educationLevel;

    @NotBlank(message = "College name is required")
    private String collegeName;

    @NotBlank(message = "Area of study is required")
    private String areaOfStudy;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
