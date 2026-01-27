package com.alphonso.Interviewer_Service.RequestDTO;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class MonthlyAvailabilityRequest {
   
    private Set<Long> skillIds;
    private List<DateSlots> dates;
}