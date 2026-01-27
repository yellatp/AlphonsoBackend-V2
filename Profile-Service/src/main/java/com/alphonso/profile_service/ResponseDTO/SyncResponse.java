package com.alphonso.profile_service.ResponseDTO;

public record SyncResponse(boolean success, Long moodleUserId, String message) {}
