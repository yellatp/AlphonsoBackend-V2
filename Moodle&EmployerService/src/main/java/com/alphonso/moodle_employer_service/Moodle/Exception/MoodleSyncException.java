package com.alphonso.moodle_employer_service.Moodle.Exception;

@SuppressWarnings("serial")
public class MoodleSyncException extends RuntimeException {
	
	public MoodleSyncException(String message) {
		super(message);
	}

	public MoodleSyncException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class AssessmentResultNotFoundException extends RuntimeException {
	    public AssessmentResultNotFoundException(String message) {
	        super(message);
	    }
	}
}
