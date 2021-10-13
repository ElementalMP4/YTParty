package main.java.de.voidtech.ytparty.service;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	
	private JavaMailSenderImpl mailSender;
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());
	
	MailService() {
		    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		    ConfigService config = new ConfigService();
		    mailSender.setHost("smtp.gmail.com");
		    mailSender.setPort(465);
		    
		    mailSender.setUsername(config.getMailUser());
		    mailSender.setPassword(config.getMailPassword());
		    
		    Properties props = mailSender.getJavaMailProperties();
		    props.put("mail.transport.protocol", "smtp");
		    props.put("mail.smtp.auth", "true");
		    props.put("mail.smtp.starttls.enable", "true");
		    props.put("mail.smtp.ssl.enable", "true");
		    props.put("mail.debug", "false");
		    
		    this.mailSender = mailSender;
	}
	
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