package com.alphonso.profile_service.ResponseDTO;

import lombok.Data;

@Data
public class PersonalInfoResponse {

	private String firstName;

	private String lastName;

	private String universityEmail;

	private String phoneNumber;

	private String address;

	private String city;

	private String state;

	private String country;

	private Integer pincode;
	
	private String linkedIn;

	private String gitHub;

	private String porfolioId;

	private String others;
}
