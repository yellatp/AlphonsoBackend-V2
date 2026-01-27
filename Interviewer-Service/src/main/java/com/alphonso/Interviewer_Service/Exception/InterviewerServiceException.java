package com.alphonso.Interviewer_Service.Exception;

public class InterviewerServiceException {

	public static class UserNotFoundException extends RuntimeException {
		public UserNotFoundException(String message) {
			super(message);
		}
	}
	
	public static class BadRequestException extends RuntimeException {
		public BadRequestException(String message) {
			super(message);
		}
	}
	 
	public static class ConflictException extends RuntimeException {
		public ConflictException(String message) {
			super(message);
		}
	}
	 
	public static class NotFoundException extends RuntimeException {
		public NotFoundException(String message) {
			super(message);
		}
	}
	 
	public static class InterviewResultNotFound extends RuntimeException {
	    public InterviewResultNotFound(String message) {
	        super(message);
	    }
	}
}
