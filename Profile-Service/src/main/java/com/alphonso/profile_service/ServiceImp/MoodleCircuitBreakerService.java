package com.alphonso.profile_service.ServiceImp;

import org.springframework.stereotype.Service;
import com.alphonso.profile_service.OpenFeign.MoodleServiceClient;
import com.alphonso.profile_service.RequestDTO.MoodleRequest;
import com.alphonso.profile_service.ResponseDTO.ApiResponse;
import com.alphonso.profile_service.ResponseDTO.ProfileMoodleResultResponse;
import com.alphonso.profile_service.ResponseDTO.SyncResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class MoodleCircuitBreakerService {

    private final MoodleServiceClient moodleClient;

    @CircuitBreaker(name = "moodleServiceCB", fallbackMethod = "syncUserFallback")
    @Retry(name = "moodleServiceRetry")
    public SyncResponse syncUser(MoodleRequest dto) {
        ApiResponse<SyncResponse> apiResp = moodleClient.syncUserToMoodle(dto);
        return apiResp != null ? apiResp.getData() : null;
    }

    public SyncResponse syncUserFallback(MoodleRequest dto, Throwable ex) {
        log.error("Moodle service DOWN while syncUser. fallback triggered. reason={}", ex.getMessage());
        return new SyncResponse(false, null, "Moodle service is currently unavailable, please try later");
    }


    @CircuitBreaker(name = "moodleServiceCB", fallbackMethod = "syncAttemptsFallback")
    @Retry(name = "moodleServiceRetry")
    public ProfileMoodleResultResponse syncAttempts(String profileId) {
        ApiResponse<ProfileMoodleResultResponse> apiResp =
                moodleClient.syncUserAttemptsUsingFeign(profileId);

        return apiResp != null ? apiResp.getData() : null;
    }

    public ProfileMoodleResultResponse syncAttemptsFallback(String profileId, Throwable ex) {
        log.error("Moodle service DOWN while syncAttempts for profileId={}, fallback triggered. reason={}",
                profileId, ex.getMessage());

        ProfileMoodleResultResponse response = new ProfileMoodleResultResponse();
        response.setStatus("MOODLE_SYNC_FAILED"); 
        return response;
    }
}
