package com.alphonso.Interviewer_Service.ResponseDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private List<AvailabilitySlotResponse> slots;
    private List<Long> skillIds;
}
