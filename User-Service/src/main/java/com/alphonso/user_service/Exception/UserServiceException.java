package com.alphonso.user_service.Exception;

public class UserServiceException {

	public static class EmailAlreadyExistsException extends RuntimeException {
		public EmailAlreadyExistsException(String message) {
			super(message);
		}
	}

	public static class InvalidCredentialsException extends RuntimeException {

		public InvalidCredentialsException(String message) {
			super(message);
		}
	}
	
	public static class InvalidOtpException extends RuntimeException {

		public InvalidOtpException(String message) {
			super(message);
		}
	}
	
	public static class EmailNotVerifiedException extends RuntimeException {

		public EmailNotVerifiedException(String message) {
			super(message);
		}
	}
	
	public static class PasswordsNotMatchingException extends RuntimeException {

		public PasswordsNotMatchingException(String message) {
			super(message);
		}
	}
	 
}
