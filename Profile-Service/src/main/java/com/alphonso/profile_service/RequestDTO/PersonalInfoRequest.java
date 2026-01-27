package com.alphonso.profile_service.RequestDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonalInfoRequest {

	@NotBlank(message = "First name is required")
	private String firstName;

	@NotBlank(message = "Last name is required")
	private String lastName;

	private String universityEmail;

	@NotBlank(message = "Phone number is required")
	@Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 digits")
	private String phoneNumber;

	@Size(max = 100, message = "Address cannot exceed 100 characters")
	private String address;

	@Size(max = 50, message = "City name cannot exceed 50 characters")
	private String city;

	@Size(max = 50, message = "State name cannot exceed 50 characters")
	private String state;

	@Size(max = 50, message = "Country name cannot exceed 50 characters")
	private String country;

	@Min(value = 100000, message = "Pincode must be at least 6 digits")
	@Max(value = 999999, message = "Pincode must be a valid 6-digit number")
	private Integer pincode;
	
	private String linkedIn;

	private String gitHub;

	private String porfolioId;

	private String others;
}

// ----------------- SKI