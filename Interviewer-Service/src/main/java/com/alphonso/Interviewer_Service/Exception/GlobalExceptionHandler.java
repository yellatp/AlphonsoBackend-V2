package com.alphonso.Interviewer_Service.Exception;

import com.alphonso.Interviewer_Service.ResponseDTO.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex,
			WebRequest request) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> {
			errors.put(err.getField(), err.getDefaultMessage());
		});

		log.warn("Validation failed: {}", errors);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(InterviewerServiceException.UserNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleUserNotFound(InterviewerServiceException.UserNotFoundException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	@ExceptionHandler(InterviewerServiceException.InterviewResultNotFound.class)
	public ResponseEntity<ApiResponse<?>> handleInterviewResultNotFound(InterviewerServiceException.InterviewResultNotFound ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InterviewerServiceException.BadRequestException.class)
	public ResponseEntity<ApiResponse<?>> handleBadRequest(InterviewerServiceException.BadRequestException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(InterviewerServiceException.ConflictException.class)
	public ResponseEntity<ApiResponse<?>> handleConflict(InterviewerServiceException.ConflictException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(InterviewerServiceException.NotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleNotFound(InterviewerServiceException.NotFoundException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {
		log.error("Runtime exception: {}", ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
		log.error("Unexpected error: {}", ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
