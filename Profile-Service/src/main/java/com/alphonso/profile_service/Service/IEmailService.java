package com.alphonso.profile_service.Service;

public interface IEmailService {
	public void sendOtpEmail(String to, Integer otp, int expiryMinutes);
	public void registrationCandidateSuccessfullEmail(String toMail);
	public void registrationInterviewerSuccessfullEmail(String toMail);
	public void moodleQuizEnableEmail(String toMail);
	public void moodleQuizPassEmail(String toMail);
	public void moodleQuizFailEmail(String toMail);
}
