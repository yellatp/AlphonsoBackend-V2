package com.alphonso.profile_service.RequestDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoodleRequest {

	private String profileId;
	private String firstName;
	private String lastName;
	private String email;
	private List<String> skills;
}
