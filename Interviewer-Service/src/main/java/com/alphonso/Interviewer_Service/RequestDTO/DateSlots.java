package com.alphonso.Interviewer_Service.RequestDTO;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class DateSlots {
    private LocalDate date;
    private List<TimeSlot> slots;
}