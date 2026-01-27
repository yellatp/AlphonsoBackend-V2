package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

public record SyncResponse(boolean success, Long moodleUserId, String message) {}
