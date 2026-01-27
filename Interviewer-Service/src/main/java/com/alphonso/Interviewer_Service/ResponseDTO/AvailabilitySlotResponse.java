package com.alphonso.Interviewer_Service.ResponseDTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.alphonso.Interviewer_Service.Entity.AvailabilitySlot;

@Data
@NoArgsConstructor
public class AvailabilitySlotResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private AvailabilitySlot.Status status; 
    private Long interviewerId;
    private String interviewerEmail;
    private String interviewerName;

    // Constructor for query projection
    public AvailabilitySlotResponse(Long id, LocalDateTime start, LocalDateTime end, 
                                   AvailabilitySlot.Status status, Long interviewerId, 
                                   String interviewerEmail, String interviewerName) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.status = status;
        this.interviewerId = interviewerId;
        this.interviewerEmail = interviewerEmail;
        this.interviewerName = interviewerName;
    }
}
