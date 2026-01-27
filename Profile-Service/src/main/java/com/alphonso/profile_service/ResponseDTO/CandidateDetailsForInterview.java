package com.alphonso.profile_service.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CandidateDetailsForInterview {
	
    public String profileId;
    public String email;
    public String firstName;
    public String lastName;
    public String domainName;

}