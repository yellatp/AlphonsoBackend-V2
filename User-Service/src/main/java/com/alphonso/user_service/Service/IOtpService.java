package com.alphonso.user_service.Service;

public interface IOtpService {
	
	public void createAndSendOtp(String email);

	public void verifyOtp(String email, Integer otp);

	public boolean isVerified(String email);

	public void deleteOtp(String email);
}
