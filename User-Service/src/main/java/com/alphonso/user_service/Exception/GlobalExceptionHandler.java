package com.alphonso.user_service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.alphonso.user_service.DTO.ApiResponse;
import com.alphonso.user_service.Exception.UserServiceException.EmailAlreadyExistsException;
import com.alphonso.user_service.Exception.UserServiceException.EmailNotVerifiedException;
import com.alphonso.user_service.Exception.UserServiceException.InvalidCredentialsException;
import com.alphonso.user_service.Exception.UserServiceException.InvalidOtpException;
import com.alphonso.user_service.Exception.UserServiceException.PasswordsNotMatchingException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiResponse<?>> handleEmailExists(EmailAlreadyExistsException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiResponse<?>> handleInvalidCredentials(InvalidCredentialsException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ApiResponse<?>> handleInvalidOtp(InvalidOtpException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ApiResponse<?>> handleEmailNotVerified(EmailNotVerifiedException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.FORBIDDEN.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	@ExceptionHandler(PasswordsNotMatchingException.class)
	public ResponseEntity<ApiResponse<?>> handlePasswordMismatch(PasswordsNotMatchingException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
				ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred", null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
