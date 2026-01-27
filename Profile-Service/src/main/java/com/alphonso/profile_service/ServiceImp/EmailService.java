package com.alphonso.profile_service.ServiceImp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.alphonso.profile_service.Service.IEmailService;
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
    
    
    public void registrationCandidateSuccessfullEmail(String toMail) {
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
    
    public void registrationInterviewerSuccessfullEmail(String toMail) {
		try {
			String subject = "Registration Successfull";
			String text = String
					.format("Thanks for registering in Alphonso, Will let you know.");

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
    
    public void moodleQuizEnableEmail(String toMail) {
		try {
			String subject = "Assessment enable in dashboard";
			String text = String
					.format("Your assessment was enabled in Alphonso dashboard, Please login and go through the asssessment."
							+ "Let us know, If you any issues. Thanks & All The Best");

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
    
    public void moodleQuizPassEmail(String toMail) {
		try {
			String subject = "Moodle Assessment Results";
			String text = String
					.format("Your Assessment was given in the moodle you are successfully passed."
							+ "Kindly wait for the T1 round. The details will provide you shortly. Thanks & All The Best");

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
    
    public void moodleQuizFailEmail(String toMail) {
		try {
			String subject = "Moodle Assessment Results";
			String text = String
					.format("Your assessment was given in the moodle you are Failed.."
							+ "We can't processs you further round. Thanks & All The Best");

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
