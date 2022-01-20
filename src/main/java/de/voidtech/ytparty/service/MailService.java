package main.java.de.voidtech.ytparty.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	
	@Autowired
	private JavaMailSenderImpl mailSender;
	
	@Autowired
	private ConfigService configService;
	
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());
	private static final String RESET_URL = "https://ytparty.voidtech.de/forgotpassword.html";
	
	public void sendMessage(String recipient, String message, String subject) {
		Thread mailThread = new Thread("MailSender") {
			public void run() {
				try {
					MimeMessage mimeMessage = mailSender.createMimeMessage();
					mimeMessage.addRecipients(RecipientType.TO, recipient);
					mimeMessage.setContent(message, "text/plain");
					mimeMessage.setSubject(subject);
					mimeMessage.setFrom(configService.getMailAddress());
					mailSender.send(mimeMessage);
					LOGGER.log(Level.INFO, "Sent e-mail");
				} catch (MessagingException e) {
					LOGGER.log(Level.SEVERE, "Error occurred during ServiceExecution: " + e.getMessage());
				}	
			}
		};
		mailThread.run();
	}
	
	public void sendPasswordResetMessage(String recipient) {
		if (recipient != null) sendMessage(recipient, 
				"Your password has been changed.\n\n"
				+ "If this wasn't you, click the link below to reset your password:\n"
				+ RESET_URL, "Password Changed");
	}
}