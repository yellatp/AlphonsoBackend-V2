package com.alphonso.Interviewer_Service.RequestDTO;

import lombok.Data;

@Data
public class FeedbackRequest {
    public Integer analytical;
    public Integer technical;
    public Integer design;
    public Integer execution;
    public Integer communication;
    public Integer collaboration;
    public Integer adaptability;
    public String notes;
}

