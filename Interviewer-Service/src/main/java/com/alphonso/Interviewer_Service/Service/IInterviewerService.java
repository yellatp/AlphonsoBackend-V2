package com.alphonso.Interviewer_Service.Service;

import java.util.List;
import com.alphonso.Interviewer_Service.RequestDTO.FeedbackRequest;
import com.alphonso.Interviewer_Service.RequestDTO.MonthlyAvailabilityRequest;
import com.alphonso.Interviewer_Service.RequestDTO.RadarChartDto;
import com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.DateResult;
import com.alphonso.Interviewer_Service.ResponseDTO.FeedbackResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewResultResponse;
import com.alphonso.Interviewer_Service.ResponseDTO.InterviewSummaryDto;

public interface IInterviewerService {

	public List<DateResult> addMonthlyAvailability(MonthlyAvailabilityRequest req, String callerEmail);
	
	public List<com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse> getCurrentMonthAvailability(String email);
	
	public List<InterviewSummaryDto> getTodayInterviews(String email);
	
	public List<InterviewSummaryDto> getUpcomingInterviews(String email);
	
	public FeedbackResponse saveOrUpdateFeedback(String interviewId, FeedbackRequest req);
	
	public List<FeedbackResponse> getFeedbackForCandidate(String candidateProfileId);
	
	public RadarChartDto toRadarDto(Long feedbackId);
	
	public InterviewResultResponse getInterviewResult(String candidateProfileId);
	
	public List<com.alphonso.Interviewer_Service.Entity.SkillSetDetails> getAllSkills();
	
	public com.alphonso.Interviewer_Service.ResponseDTO.InterviewerDetailsResponse getInterviewerDetails(String email);
}
