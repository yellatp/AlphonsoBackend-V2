package com.alphonso.Interviewer_Service.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;
import com.alphonso.Interviewer_Service.Entity.Feedback;
import lombok.Data;

@Data
public class FeedbackResponse {
    public Long id;
    public String candidateProfileId;
    public String interviewerId;
    public String interviewId;
    public Map<String, Integer> ratings;
    public Double averageScore;
    public Feedback.Recommendation recommendation;
    public String notes;
    public LocalDateTime createdAt;
}