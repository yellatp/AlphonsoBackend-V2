package com.alphonso.user_service.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.alphonso.user_service.DTO.*;
import com.alphonso.user_service.Service.IOtpService;
import com.alphonso.user_service.Service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final IUserService userService;
    private final IOtpService otpService;

    @Value("${google.client-ids}")
    private String googleClientId;

    public UserController(IUserService authService, IOtpService otpService) {
        this.userService = authService;
        this.otpService = otpService;
    }
    
    @PostMapping("/create/**")
    public ResponseEntity<LoginResponse> register(
            HttpServletRequest request,
            @RequestBody CreateUserRequest req) {

        log.info("Received registration request for email: {}", req.getEmail());

        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        if (parts.length >= 7) {
            return ResponseEntity.badRequest().build();
        }

        String role = parts[4];
        log.info("Role extracted: {}", role);

        LoginResponse resp= userService.register(role, req);

        log.info("User created successfully: {}", req.getEmail());

        return ResponseEntity.ok(resp);
    }

    
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> google(@Valid @RequestBody GoogleRequest req) {
        log.info("Google login request received");
        LoginResponse response = userService.loginWithGoogle(req);
        log.info("Google login successful for email: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("Login attempt for email: {}", req.getEmail());
        LoginResponse response = userService.login(req);
        log.info("Login successful for email: {}", req.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody SendOtpRequest req) {
        log.info("Received forgot password request for {}", req.getEmail());
        try {
            otpService.createAndSendOtp(req.getEmail());
            log.info("OTP sent successfully to {}", req.getEmail());
            return ResponseEntity.ok("OTP sent successfully to your email.");
        } catch (Exception e) {
            log.error("Error occurred while sending OTP for {}: {}", req.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        log.info("Received password reset request for {}", req.getEmail());
        try {
            userService.resetPassword(req);
            log.info("Password reset successfully for {}", req.getEmail());
            return ResponseEntity.ok("Password reset successfully. Please log in with your new password.");
        } catch (Exception e) {
            log.error("Error while resetting password for {}: {}", req.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user details for email: {}", email);
        UserDTO userDTO = userService.getUserByEmail(email);
        log.info("User details fetched successfully for email: {}", email);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Health check ping received");
        return ResponseEntity.ok("auth-service ok");
    }
}
