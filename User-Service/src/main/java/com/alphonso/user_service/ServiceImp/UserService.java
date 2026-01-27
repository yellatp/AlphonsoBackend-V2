package com.alphonso.user_service.ServiceImp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alphonso.user_service.DTO.CreateUserRequest;
import com.alphonso.user_service.DTO.GoogleRequest;
import com.alphonso.user_service.DTO.LoginRequest;
import com.alphonso.user_service.DTO.LoginResponse;
import com.alphonso.user_service.DTO.ResetPasswordRequest;
import com.alphonso.user_service.DTO.UserDTO;
import com.alphonso.user_service.Exception.UserServiceException.EmailAlreadyExistsException;
import com.alphonso.user_service.Exception.UserServiceException.EmailNotVerifiedException;
import com.alphonso.user_service.Exception.UserServiceException.InvalidCredentialsException;
import com.alphonso.user_service.Exception.UserServiceException.PasswordsNotMatchingException;
import com.alphonso.user_service.Model.EmailOtp;
import com.alphonso.user_service.Model.RoleCategory;
import com.alphonso.user_service.Model.UsersReg;
import com.alphonso.user_service.Repository.EmailOtpRepository;
import com.alphonso.user_service.Repository.RoleRepository;
import com.alphonso.user_service.Repository.UserRepository;
import com.alphonso.user_service.Service.IEmailService;
import com.alphonso.user_service.Service.IUserService;
import com.alphonso.user_service.Util.GoogleTokenVerifier;
import com.alphonso.user_service.Util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements IUserService {

	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final EmailOtpRepository emailRepo;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final TokenGenerator tokenGenerator;
	private final GoogleTokenVerifier googleVerifier;
	private final OtpService otpService;
	private final List<String> allowedClientIds;

	public UserService(UserRepository repo, RoleRepository roleRepo, EmailOtpRepository emailRepo,
			IEmailService emailService,
			TokenGenerator tokenGenerator, GoogleTokenVerifier googleVerifier,
			@Value("${google.client-ids}") String clientIds, OtpService otpService) {

		this.userRepo = repo;
		this.roleRepo = roleRepo;
		this.emailRepo = emailRepo;
		this.tokenGenerator = tokenGenerator;
		this.googleVerifier = googleVerifier;
		this.allowedClientIds = Arrays.stream(clientIds.split(",")).map(String::trim).collect(Collectors.toList());
		this.otpService = otpService;
		log.info("UserService initialized with {} allowed Google client IDs", allowedClientIds.size());
	}

	@Transactional
	public LoginResponse register(String role, CreateUserRequest req) {
		String email = req.getEmail().toLowerCase().trim();
		log.info("Starting registration process for email: {}", email);

		if (userRepo.findByEmail(email).isPresent()) {
			log.warn("Registration failed — email already exists: {}", email);
			throw new EmailAlreadyExistsException("Email already registered: " + email);
		}

		RoleCategory assignedRole;
		try {
			String inputRole = role;
			String normalizedRole = (inputRole != null) ? inputRole.toUpperCase() : "CANDIDATE";

			if (!List.of("ADMIN", "EMPLOYER", "INTERVIEWER", "CANDIDATE").contains(normalizedRole)) {
				log.warn("Invalid role attempted during registration: {}", normalizedRole);
				throw new IllegalArgumentException(
						"Invalid role. Allowed values: ADMIN, EMPLOYER, INTERVIEWER, CANDIDATE");
			}

			assignedRole = roleRepo.findByCategoryName(normalizedRole)
					.orElseThrow(() -> new IllegalArgumentException("Role not found in database: " + normalizedRole));

		} catch (IllegalArgumentException ex) {
			log.error("Role validation failed during registration for {}: {}", email, ex.getMessage());
			throw new IllegalArgumentException(
					"Invalid role. Allowed values: ADMIN, EMPLOYER, INTERVIEWER, CANDIDATE");
		}

		otpService.createAndSendOtp(req.getEmail());
        log.info("OTP sent successfully to: {}", req.getEmail());
		
		UsersReg user = UsersReg.builder()
				.email(email)
				.firstName(req.getFirstName())
				.lastName(req.getLastName())
				.password(passwordEncoder.encode(req.getPassword()))
				.provider(UsersReg.Provider.LOCAL)
				.isVerified(false)
				.category(assignedRole)
				.build();

		UsersReg saved = userRepo.save(user);

		
		String token = tokenGenerator.generateToken(saved.getEmail(), saved.getId(), saved.getFirstName(), saved.getLastName(),
				assignedRole.getCategoryName());
		log.info("User registered successfully: {}", email);
		return new LoginResponse(token, saved.getId(), saved.getEmail(), assignedRole.getCategoryName());
	}

	@Transactional
	public LoginResponse loginWithGoogle(GoogleRequest req) {
		log.info("Google login attempt initiated");

		Map<String, Object> tokenInfo;
		try {
			tokenInfo = googleVerifier.verify(req.getIdToken());
		} catch (RuntimeException e) {
			log.error("Google token verification failed: {}", e.getMessage(), e);
			throw new InvalidCredentialsException("Failed to verify Google token. Please try again.");
		}

		if (tokenInfo == null || tokenInfo.isEmpty()) {
			log.error("Google login failed — invalid or empty token info");
			throw new InvalidCredentialsException("Invalid Google token");
		}

		Object aud = tokenInfo.get("aud");
		if (aud == null || !allowedClientIds.contains(aud.toString())) {
			log.warn("Google login failed — client ID not allowed: {}", aud);
			throw new InvalidCredentialsException("Google token audience mismatch");
		}

		boolean emailVerified = Boolean.parseBoolean(String.valueOf(tokenInfo.getOrDefault("email_verified", "false")));
		if (!emailVerified) {
			log.warn("Google login failed — email not verified");
			throw new InvalidCredentialsException("Google email not verified");
		}

		RoleCategory customerRole;
		try {
			customerRole = roleRepo.findByCategoryName("CANDIDATE")
					.orElseThrow(() -> new IllegalStateException("Default role 'CANDIDATE' not found in database"));
		} catch (IllegalStateException e) {
			log.error("Database configuration error: {}", e.getMessage(), e);
			throw new RuntimeException("System configuration error. Please contact support.");
		}

		String email = ((String) tokenInfo.get("email")).toLowerCase();
		String givenName = (String) tokenInfo.getOrDefault("given_name", "");
		String familyName = (String) tokenInfo.getOrDefault("family_name", "");

		UsersReg user = userRepo.findByEmail(email).orElseGet(() -> {
			log.info("Creating new Google user: {}", email);
			// Google already verifies emails, so set isVerified to true
			return userRepo.save(UsersReg.builder()
					.email(email)
					.firstName(givenName)
					.lastName(familyName)   
					.provider(UsersReg.Provider.GOOGLE)
					.isVerified(true) // Google emails are already verified
					.category(customerRole)
					.build());
		});

		// If existing user is not verified but logging in with Google, mark as verified
		// since Google has already verified their email
		if (!user.isVerified() && user.getProvider() == UsersReg.Provider.GOOGLE) {
			log.info("Marking existing Google user as verified: {}", email);
			user.setVerified(true);
			userRepo.save(user);
		}

		String token = tokenGenerator.generateToken(user.getEmail(), user.getId(), user.getFirstName(), user.getLastName(),
				customerRole.getCategoryName());
		log.info("Google login completed successfully for {}", email);
		return new LoginResponse(token, user.getId(), user.getEmail(), customerRole.getCategoryName());
	}

	public LoginResponse login(LoginRequest req) {
		String email = req.getEmail().toLowerCase().trim();
		log.info("Login attempt for email: {}", email);

		UsersReg user = userRepo.findByEmail(email).orElseThrow(() -> {
			log.warn("Login failed — user not found: {}", email);
			return new InvalidCredentialsException("Invalid credentials");
		});

		if(user.isVerified()) {
			if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
				log.warn("Login failed — invalid password for email: {}", email);
				throw new InvalidCredentialsException("Invalid credentials");
			}

			String token = tokenGenerator.generateToken(user.getEmail(), user.getId(), user.getFirstName(), user.getLastName(),
					user.getCategory().getCategoryName());
			log.info("Login successful for email: {}", email);
			return new LoginResponse(token, user.getId(), user.getEmail(), user.getCategory().toString());
		}

		else {
			throw new InvalidCredentialsException("User EmailId not verified");
		}
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest req) {
		String email = req.getEmail().toLowerCase().trim();
		log.info("Password reset attempt for email: {}", email);

		UsersReg user = userRepo.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Password reset failed — email not registered: {}", email);
					return new EmailNotVerifiedException("Email not registered. Please register first.");
				});

		emailRepo.findByEmail(email).filter(EmailOtp::isVerified).orElseThrow(() -> {
			log.warn("Password reset blocked — email not verified: {}", email);
			return new EmailNotVerifiedException("Email not verified. Please verify OTP before reset.");
		});

		if (!otpService.isVerified(email)) {
			log.warn("Password reset failed — OTP not verified for {}", email);
			throw new EmailNotVerifiedException("Email OTP not verified");
		}

		if (!req.getNewPassword().equals(req.getConfirmPassword())) {
			log.warn("Password reset failed — newPassword and confirmPassword mismatch for {}", email);
			throw new PasswordsNotMatchingException("Passwords do not match (case-sensitive check)");
		}

		user.setPassword(passwordEncoder.encode(req.getNewPassword()));
		userRepo.save(user);

		log.info("Password successfully reset for {}", email);
	}


	@Override
	public UserDTO getUserByEmail(String email) {
		log.info("Fetching user details for email: {}", email);
		UsersReg user = userRepo.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("User not found for email: {}", email);
					return new UsernameNotFoundException("User not found with email: " + email);
				});

		log.info("User details retrieved successfully for {}", email);
		return UserDTO.builder()
				.id(user.getId())
				.email(user.getEmail())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.category(user.getCategory().getCategoryName())
				.build();
	}
}
