package com.alphonso.user_service.Service;

public interface IEmailService {
	public void sendOtpEmail(String to, Integer otp, int expiryMinutes);
	public void registrationSuccessfullEmail(String toMail);
}
