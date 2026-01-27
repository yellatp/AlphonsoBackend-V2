package com.alphonso.user_service.ServiceImp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alphonso.user_service.Model.EmailOtp;
import com.alphonso.user_service.Repository.EmailOtpRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpAttemptService {

    private final EmailOtpRepository otpRepo;

    public OtpAttemptService(EmailOtpRepository otpRepo) {
        this.otpRepo = otpRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempts(EmailOtp record) {
        int currentAttempts = record.getAttempts();
        record.setAttempts(currentAttempts + 1);
        otpRepo.save(record);
        log.warn("Incremented OTP attempts for email: {} (attempt {}/{})",
                record.getEmail(), record.getAttempts(), "max configured in properties");
    }
}
