package com.alphonso.profile_service.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class FeedbackResponse {
    private Long id;
    private String candidateProfileId;
    private String interviewerId;
    private String interviewId;
    private Map<String, Integer> ratings;
    private Double averageScore;
    private String recommendation; // "YES" or "NO"
    private String notes;
    private LocalDateTime createdAt;
}
