package com.alphonso.profile_service.ResponseDTO;

import java.util.List;
import lombok.Data;

@Data
public class ProfileSkillResponse {
	private String groupName;
    private String roleName;
    private List<String> coreSkillNames;
    private List<String> additionalSkillNames;
    private String programmingName;
}
