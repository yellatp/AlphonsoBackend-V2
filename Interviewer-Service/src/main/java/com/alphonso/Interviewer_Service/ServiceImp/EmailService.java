package com.alphonso.Interviewer_Service.ServiceImp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Value("${spring.mail.username}")
	private String fromAddress;

	public void sendSimpleMail(String to, String subject, String body) {
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(body);
			mailSender.send(msg);
			log.info("Sent reminder mail to {}", to);
		} catch (Exception ex) {
			log.error("Failed to send mail to {}: {}", to, ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	public void sendMorningReminder(String to, String name) {
	    String subject = "Reminder: Update Monthly Availability";
	    String body = String.format(
	            "Hi %s,\n\n"
	            + "This is a reminder to add your availability slots for next month.\n\n"
	            + "Please log in and add your available dates/times before the month starts.\n\n"
	            + "Thank you,\nRecruitment Team",
	            name
	    );
	    sendSimpleMail(to, subject, body);
	}

	public void sendAfternoonReminder(String to, String name) {
	    String subject = "Final Reminder: Update Availability Slots";
	    String body = String.format(
	            "Hi %s,\n\n"
	            + "This is a final reminder for today.\n"
	            + "If you have already updated your availability slots, kindly ignore this email.\n"
	            + "Otherwise, please update them before the month ends.\n\n"
	            + "Thank you,\nRecruitment Team",
	            name
	    );
	    sendSimpleMail(to, subject, body);
	}


	public void sendCandidateBookedEmail(String toMail, String candidateName, String interviewCode, String start,
			String end, String meetUrl) {
		String subject = "Interview scheduled: " + interviewCode;
		String body = String.format("Hi %s,\n\n"
				+ "Congratulations — you have cleared the initial technical assessment. You have been scheduled for the next technical round.\n\n"
				+ "Interview ID: %s\n" + "Time: %s to %s\n\n"
				+ "Please join via this link when the interview starts:\n%s\n\n"
				+ "You can also log in to the portal to see your scheduled slots and further instructions.\n\n"
				+ "Good luck,\nRecruitment Team", candidateName, interviewCode, start, end, meetUrl);

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(fromAddress);
		msg.setTo(toMail);
		msg.setSubject(subject);
		msg.setText(body);
		mailSender.send(msg);
		log.info("Candidate email sent to {} for interview {}", toMail, interviewCode);
	}

	public void sendInterviewerScheduledEmail(String toMail, String interviewerName, String interviewCode, String start,
			String end, String meetUrl) {
		String subject = "New interview scheduled: " + interviewCode;
		String body = String.format("Hi %s,\n\n" + "A candidate has been scheduled for a technical interview.\n\n"
				+ "Interview ID: %s\n" + "Time: %s to %s\n\n"
				+ "Please join the interview at the scheduled time using the link below:\n%s\n\n"
				+ "Please evaluate the candidate and update the portal with time-slot availability for future interviews if needed.\n\n"
				+ "Thanks,\nRecruitment Team", interviewerName, interviewCode, start, end, meetUrl);

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(fromAddress);
		msg.setTo(toMail);
		msg.setSubject(subject);
		msg.setText(body);
		mailSender.send(msg);
		log.info("Interviewer email sent to {} for interview {}", toMail, interviewCode);
	}

}