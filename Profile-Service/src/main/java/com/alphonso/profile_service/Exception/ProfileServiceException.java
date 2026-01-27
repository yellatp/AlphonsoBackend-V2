package com.alphonso.profile_service.Exception;

public class ProfileServiceException {

	public static class ProfileNotFoundException extends RuntimeException {
		public ProfileNotFoundException(String message) {
			super(message);
		}
	}

	public static class UserNotFoundException extends RuntimeException {
		public UserNotFoundException(String message) {
			super(message);
		}
	}

	public static class UnauthorizedAccessException extends RuntimeException {
		public UnauthorizedAccessException(String message) {
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

}
