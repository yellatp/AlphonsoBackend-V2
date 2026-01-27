package com.alphonso.profile_service.Service;

import com.alphonso.profile_service.Entity.ProfileDetails;

public interface IOtpService {

	public void createAndSendOtp(String universityEmail, String roles);
	public boolean verifyOtp(String universityEmail, Integer otp);
	public boolean isAlreadyExist(String email);
	public boolean isVerified(String email);
	public void linkProfile(String universityEmail, ProfileDetails profile);
	public void deleteOtp(String email);
}
