package com.alphonso.Interviewer_Service.ResponseDTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InterviewSummaryDto {

    private String interviewId;
    private String domain;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetUrl;
}
