package com.alphonso.profile_service.OpenFeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.alphonso.profile_service.RequestDTO.MoodleRequest;
import com.alphonso.profile_service.ResponseDTO.ApiResponse;
import com.alphonso.profile_service.ResponseDTO.ProfileMoodleResultResponse;
import com.alphonso.profile_service.ResponseDTO.SyncResponse;

@FeignClient(name = "moodle-service",
configuration = com.alphonso.profile_service.Security.FeignClientInterceptor.class)
public interface MoodleServiceClient {
	
	@PostMapping("/api/moodle/sync-user")
	ApiResponse<SyncResponse> syncUserToMoodle(@RequestBody MoodleRequest profileMoodleRequest);

    
    @PostMapping("/api/moodle/sync-profile-results")
    ApiResponse<ProfileMoodleResultResponse> syncUserAttemptsUsingFeign(
            @RequestParam String profileId
    );

}

