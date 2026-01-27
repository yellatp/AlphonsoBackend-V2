package com.alphonso.user_service.ServiceImp;

import com.alphonso.user_service.Exception.UserServiceException.InvalidOtpException;
import com.alphonso.user_service.Model.EmailOtp;
import com.alphonso.user_service.Repository.EmailOtpRepository;
import com.alphonso.user_service.Repository.UserRepository;
import com.alphonso.user_service.Service.IOtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
public class OtpService implements IOtpService {

	private final UserRepository userRepository;
	private final EmailOtpRepository otpRepo;
	private final EmailService emailService;
	private final SecureRandom secureRandom = new SecureRandom();
	private final OtpAttemptService otpAttemptService;
	private final int otpLength;
	private final int expiryMinutes;
	private final int maxAttempts;

	public OtpService(UserRepository userRepository, EmailOtpRepository otpRepo, EmailService emailService,
			@Value("${otp.length:6}") int otpLength, @Value("${otp.expiry-minutes:1}") int expiryMinutes,
			@Value("${otp.max-attempts:5}") int maxAttempts, OtpAttemptService otpAttemptService) {
		this.userRepository = userRepository;
		this.otpRepo = otpRepo;
		this.emailService = emailService;
		this.otpLength = otpLength;
		this.expiryMinutes = expiryMinutes;
		this.maxAttempts = maxAttempts;
		this.otpAttemptService = otpAttemptService;
	}

	private Integer generateOtp() {
		int min = (int) Math.pow(10, otpLength - 1);
		int max = (int) Math.pow(10, otpLength) - 1;
		return secureRandom.nextInt(max - min + 1) + min;
	}

	@Transactional
	public void createAndSendOtp(String email) {
		log.info("Initiating OTP creation for email: {}", email);
		if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			log.warn("Invalid email format provided: {}", email);
			throw new IllegalArgumentException("Invalid email format");
		}

		// Prevent OTP sending for Google users - Google already verifies emails
		userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
			if (user.getProvider() == com.alphonso.user_service.Model.UsersReg.Provider.GOOGLE) {
				log.info("Skipping OTP for Google user: {} - Google already verified their email", email);
				throw new IllegalStateException("OTP not required for Google users. Email is already verified by Google.");
			}
		});

		Optional<EmailOtp> existing = otpRepo.findByEmail(email);
		if (existing.isPresent()) {
			EmailOtp record = existing.get();
			if (LocalDateTime.now(ZoneId.of("Asia/Kolkata")).isBefore(record.getExpiresAt())) {
				log.warn("OTP already sent to {}. Waiting for expiry.", email);
				throw new IllegalStateException("OTP already sent. Please wait until it expires.");
			}
		}

		Integer otp = generateOtp();
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
		LocalDateTime expiry = now.plusMinutes(expiryMinutes);

		EmailOtp entity = existing.map(e -> {
			e.setOtp(otp);
			e.setCreatedAt(now);
			e.setExpiresAt(expiry);
			e.setAttempts(0);
			if (!e.isVerified()) {
				e.setVerified(false);
			}
			return e;
		}).orElseGet(() -> new EmailOtp(email, otp, now, expiry));

		otpRepo.save(entity);
		log.info("OTP generated and saved for email: {} (expires in {} minutes)", email, expiryMinutes);

		try {
			emailService.sendOtpEmail(email, otp, expiryMinutes);
			log.info("OTP email successfully sent to {}", email);
		} catch (Exception ex) {
			log.error("Failed to send OTP email to {}: {}", email, ex.getMessage(), ex);
		}
	}

	@Transactional
	public void verifyOtp(String email, Integer otp) {
	    log.info("Verifying OTP for email: {}", email);

	    EmailOtp record = otpRepo.findByEmail(email).orElseThrow(() -> {
	        log.warn("No OTP found for email: {}", email);
	        return new InvalidOtpException("No OTP requested for this email");
	    });

	    if (record.isVerified()) {
	        log.warn("OTP already verified for email: {}", email);
	        throw new InvalidOtpException("OTP already verified for this email");
	    }

	    if (LocalDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(record.getExpiresAt())) {
	        log.warn("Expired OTP for email: {}", email);
	        throw new InvalidOtpException("OTP expired");
	    }

	    if (record.getAttempts() >= maxAttempts) {
	        log.warn("Max OTP attempts exceeded for email: {}", email);
	        throw new InvalidOtpException("Max OTP attempts exceeded");
	    }

	    if (!record.getOtp().equals(otp)) {
	        log.warn("Invalid OTP attempt for email: {}", email);
	        otpAttemptService.incrementAttempts(record);
	        throw new InvalidOtpException("Invalid OTP");
	    }

	    record.setVerified(true);
	    otpRepo.save(record);
	    log.info("OTP successfully verified for email: {}", email);

	    userRepository.findByEmail(email).ifPresent(user -> {
	        user.setVerified(true);
	        userRepository.save(user);
	        log.info("User marked as verified for email: {}", email);

	        try {
	                emailService.registrationSuccessfullEmail(email);
	                log.info("Registration success email sent to Google user: {}", email);

	        } catch (Exception e) {
	            log.error("Failed to send registration success email to {}: {}", email, e.getMessage(), e);
	        }
	    });
	}

	public boolean isVerified(String email) {
		boolean verified = otpRepo.findByEmail(email).map(EmailOtp::isVerified).orElse(false);
		log.debug("Email verification check for {} -> {}", email, verified);
		return verified;
	}
	
	

	@Transactional
	public void deleteOtp(String email) {
		otpRepo.deleteByEmail(email);
		log.info("Deleted OTP record for email: {}", email);
	}
}
