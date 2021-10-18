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
	
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());
	
	public void sendMessage(String recipient, String message, String subject) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			mimeMessage.addRecipients(RecipientType.TO, recipient);
			mimeMessage.setContent(message, "text/plain");
			mimeMessage.setSubject(subject);
			mailSender.send(mimeMessage);
			LOGGER.log(Level.INFO, "Sent e-mail");
		} catch (MessagingException e) {
			LOGGER.log(Level.SEVERE, "Error occurred during ServiceExecution: " + e.getMessage());
		}
	}
}