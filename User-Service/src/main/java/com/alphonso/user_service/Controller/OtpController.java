package com.alphonso.user_service.Controller;

import com.alphonso.user_service.DTO.SendOtpRequest;
import com.alphonso.user_service.DTO.VerifyOtpRequest;
import com.alphonso.user_service.Service.IOtpService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final IOtpService otpService;

    public OtpController(IOtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest req) {
        log.info("Received OTP send request for email: {}", req.getEmail());
        try {
            otpService.createAndSendOtp(req.getEmail());
            log.info("OTP sent successfully to: {}", req.getEmail());
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("Failed to send OTP to {}: {}", req.getEmail(), ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while sending OTP to {}: {}", req.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        log.info("Received OTP verification request for email: {}", req.getEmail());
        try {
            otpService.verifyOtp(req.getEmail(), req.getOtp());
            log.info("OTP successfully verified for email: {}", req.getEmail());
            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } catch (Exception e) {
            log.error("OTP verification failed for {}: {}", req.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
