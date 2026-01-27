package com.alphonso.profile_service.ServiceImp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alphonso.profile_service.Entity.ProfileDetails;
import com.alphonso.profile_service.Entity.UniversityEmailOtp;
import com.alphonso.profile_service.Exception.ProfileServiceException.NotFoundException;
import com.alphonso.profile_service.Repository.UniversityEmailOtpRepository;
import com.alphonso.profile_service.Service.IOtpService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
public class OtpService implements IOtpService{

    private final UniversityEmailOtpRepository otpRepo;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int otpLength;
    private final int expiryMinutes;
    private final int maxAttempts;

    public OtpService(
            UniversityEmailOtpRepository otpRepo,
            EmailService emailService,
            @Value("${otp.length:6}") int otpLength,
            @Value("${otp.expiry-minutes:5}") int expiryMinutes,
            @Value("${otp.max-attempts:5}") int maxAttempts) {

        this.otpRepo = otpRepo;
        this.emailService = emailService;
        this.otpLength = otpLength;
        this.expiryMinutes = expiryMinutes;
        this.maxAttempts = maxAttempts;
    }

    private Integer generateOtp() {
        int min = (int) Math.pow(10, otpLength - 1); 
        int max = (int) Math.pow(10, otpLength) - 1; 
        return secureRandom.nextInt(max - min + 1) + min;
    }

    
    @Transactional
    public void createAndSendOtp(String universityEmail, String roles) {
        log.info("Initiating OTP creation for university email: {}", universityEmail);

        if (roles.equalsIgnoreCase("CANDIDATE")) {
        	 // Allow both .edu domains and gmail.com for university email
        	 if (universityEmail == null || !(universityEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.edu$") 
        	     || universityEmail.matches("^[A-Za-z0-9+_.-]+@gmail\\.com$"))) {
                 throw new IllegalArgumentException("Invalid university email. Only .edu domains or gmail.com are allowed.");
             }
	    } 
        
        Optional<UniversityEmailOtp> existing = otpRepo.findByEmail(universityEmail);
        if (existing.isPresent()) {
            UniversityEmailOtp record = existing.get();
            
            if (record.isVerified()) {
                throw new IllegalStateException(
                    "This university email is already verified. Duplicate emails are not allowed."
                );
            }
            
            if (LocalDateTime.now(ZoneId.of("Asia/Kolkata")).isBefore(record.getExpiresAt())) {
                throw new IllegalStateException("OTP already sent. Please wait until it expires.");
            }
        }

        Integer otp = generateOtp();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime expiry = now.plusMinutes(expiryMinutes);

        UniversityEmailOtp entity = existing.map(e -> {
            e.setOtp(otp);
            e.setCreatedAt(now);
            e.setExpiresAt(expiry);
            e.setVerified(false);
            e.setAttempts(0);
            return e;
        }).orElseGet(() -> new UniversityEmailOtp(universityEmail, otp, now, expiry));

        otpRepo.save(entity);
        log.info("OTP generated and saved for email: {} (expires in {} minutes)", universityEmail, expiryMinutes);

        try {
            emailService.sendOtpEmail(universityEmail, otp, expiryMinutes);
            log.info("OTP email successfully sent to {}", universityEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", universityEmail, ex.getMessage(), ex);
        }
    }

    @Transactional
    public boolean verifyOtp(String universityEmail, Integer otp) {
        log.info("Verifying OTP for university email: {}", universityEmail);

        UniversityEmailOtp record = otpRepo.findByEmail(universityEmail)
                .orElseThrow(() -> new IllegalArgumentException("No OTP requested for this email"));

        if (record.isVerified()) {
            throw new IllegalStateException("OTP already verified");
        }

        if (LocalDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(record.getExpiresAt())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (record.getAttempts() >= maxAttempts) {
            throw new IllegalArgumentException("Max OTP attempts exceeded");
        }

        if (!record.getOtp().equals(otp)) {
            record.setAttempts(record.getAttempts() + 1);
            otpRepo.save(record);
            throw new IllegalArgumentException("Invalid OTP");
        }

        record.setVerified(true);
        otpRepo.save(record);

        ProfileDetails profile = (ProfileDetails)record.getProfile();
        if (profile != null) {
            profile.setUniversityEmailVerified(true);
        }

        log.info("OTP successfully verified for email: {}", universityEmail);
        return true; 
    }

    public boolean isAlreadyExist(String email) {
    	return otpRepo.existsByEmail(email);
    }
    
    public boolean isVerified(String email) {
        return otpRepo.findByEmail(email).map(UniversityEmailOtp::isVerified).orElse(false);
    }

    
    @Transactional
    public void linkProfile(String universityEmail, ProfileDetails profile) {
        UniversityEmailOtp otp = otpRepo.findByEmail(universityEmail)
                .orElseThrow(() -> new NotFoundException("OTP record not found for email: " + universityEmail));

        otp.setProfile(profile);
        otpRepo.save(otp);
    }

    
    @Transactional
    public void deleteOtp(String email) {
        otpRepo.deleteByEmail(email);
        log.info("Deleted OTP record for email: {}", email);
    }
}
