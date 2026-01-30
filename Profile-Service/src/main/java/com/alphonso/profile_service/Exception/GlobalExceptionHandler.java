package com.alphonso.profile_service.Exception;

import com.alphonso.profile_service.Exception.ProfileServiceException.BadRequestException;
import com.alphonso.profile_service.Exception.ProfileServiceException.ConflictException;
import com.alphonso.profile_service.Exception.ProfileServiceException.NotFoundException;
import com.alphonso.profile_service.Exception.ProfileServiceException.ProfileNotFoundException;
import com.alphonso.profile_service.Exception.ProfileServiceException.UnauthorizedAccessException;
import com.alphonso.profile_service.Exception.ProfileServiceException.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.alphonso.profile_service.ResponseDTO.ApiResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<?>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
		log.warn("Invalid request body: {}", ex.getMessage());
		String message = "Invalid request body. Ensure JSON is valid and includes required fields (e.g. email for send-otp).";
		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message, null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiResponse<?>> handleMissingHeader(MissingRequestHeaderException ex) {
		log.warn("Missing required header: {}", ex.getMessage());
		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
				"Missing required header: " + ex.getHeaderName(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(ProfileNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleProfileNotFound(ProfileNotFoundException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ApiResponse<?>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.FORBIDDEN.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiResponse<?>> handleConflict(ConflictException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {

		log.error("Runtime exception occurred: {}", ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
				ex.getMessage() != null ? ex.getMessage() : "Runtime error", null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {

		log.error("Unexpected error: {}", ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
