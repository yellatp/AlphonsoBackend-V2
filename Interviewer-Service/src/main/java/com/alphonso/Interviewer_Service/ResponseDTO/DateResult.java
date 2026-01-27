package com.alphonso.Interviewer_Service.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DateResult {
    private LocalDate date;
    private String status;
    private List<String> createdSlots;
    private List<String> conflicts;
}