package com.alphonso.user_service.ServiceImp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.alphonso.user_service.Service.IEmailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendOtpEmail(String to, Integer otp, int expiryMinutes) {
        log.info("Preparing to send OTP email to: {}", to);
        try {
            String subject = "Your verification code";
            String text = String.format(
                    "Your verification code is: %s%nThis code expires in %d minutes.%nIf you didn't request this code, ignore this email.",
                    otp, expiryMinutes);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);

            mailSender.send(msg);
            log.info("OTP email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }
    
    
    @Override
    public void registrationSuccessfullEmail(String toMail) {
		try {
			String subject = "Registration Successfull";
			String text = String
					.format("Thanks for registering in Alphonso, Your application under review. Will let you know.");

			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(fromAddress);
			msg.setTo(toMail);
			msg.setSubject(subject);
			msg.setText(text);

			mailSender.send(msg);
			log.info("Registration completion email sent successfully to: {}", toMail);
		} catch (Exception e) {
			log.error("Failed to send Registration completion email to {}. Error: {}", toMail, e.getMessage(), e);
			throw e;
		}
	}
}
