package com.alphonso.moodle_employer_service.Employer.Exception;

public class EmployerServiceException {

	public static class CandidateProfileViewException extends RuntimeException {
		public CandidateProfileViewException(String message) {
			super(message);
		}
	}
	
	public static class PipelineException extends RuntimeException {
	    public PipelineException(String message) {
	        super(message);
	    }
	}
	
	public static class UnauthorizedEmployerException extends RuntimeException {
	    public UnauthorizedEmployerException(String message) {
	        super(message);
	    }
	}

}
