package com.alphonso.moodle_employer_service.Moodle.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.CandidateProfileViewException;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.PipelineException;
import com.alphonso.moodle_employer_service.Employer.Exception.EmployerServiceException.UnauthorizedEmployerException;
import com.alphonso.moodle_employer_service.Moodle.Exception.MoodleSyncException.AssessmentResultNotFoundException;
import com.alphonso.moodle_employer_service.Moodle.ResponseDTO.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex,
			WebRequest request) {

		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
			String errorMessage = Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value");
			fieldErrors.put(fieldName, errorMessage);
		});

		log.warn("Validation failed for request {}: {}", request.getDescription(false), fieldErrors);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MoodleSyncException.class)
	public ResponseEntity<ApiResponse<?>> handleMoodleSyncException(MoodleSyncException ex, WebRequest request) {
		log.error("MoodleSyncException at {}: {}", request.getDescription(false), ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(AssessmentResultNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleAssessmentResultNotFoundException(AssessmentResultNotFoundException ex,
			WebRequest request) {
		log.error("MoodleSyncException at {}: {}", request.getDescription(false), ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(CandidateProfileViewException.class)
	public ResponseEntity<ApiResponse<?>> handleCandidateProfileViewException(CandidateProfileViewException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(PipelineException.class)
	public ResponseEntity<ApiResponse<?>> handlePipelineException(PipelineException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UnauthorizedEmployerException.class)
	public ResponseEntity<ApiResponse<?>> handleUnauthorizedEmployerException(UnauthorizedEmployerException ex) {

		ApiResponse<?> response = ApiResponse.error(HttpStatus.FORBIDDEN.value(), ex.getMessage(), null);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex, WebRequest request) {
		log.error("Unexpected error for {}: {}", request.getDescription(false), ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {
		log.error("Runtime exception: {}", ex.getMessage(), ex);

		ApiResponse<?> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

}
