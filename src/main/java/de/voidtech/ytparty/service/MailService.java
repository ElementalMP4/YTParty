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
	private JavaMailSenderImpl mailSender; //Inject the mail sender bean from YTParty.java
	
	@Autowired
	private ConfigService configService; //We need the config to get the sending address
	
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName()); //Get the logger
	private static final String RESET_URL = "https://ytparty.voidtech.de/forgotpassword.html";
	//Users will go to this URL to reset their password. This URL will be emailed to them.
	
	public void sendMessage(String recipient, String message, String subject) {
		Thread mailThread = new Thread("MailSender") { //Create a new thread to send emails in so that the main thread is not held up
			public void run() {
				try {
					MimeMessage mimeMessage = mailSender.createMimeMessage(); //Create a new message object
					mimeMessage.addRecipients(RecipientType.TO, recipient); //Set the recipient
					mimeMessage.setContent(message, "text/plain"); //Set the message content MIME type
					mimeMessage.setSubject(subject); //Set the subject
					mimeMessage.setFrom(configService.getMailAddress()); //Set the sender
					mailSender.send(mimeMessage); //Send the message
					LOGGER.log(Level.INFO, "Sent e-mail"); //Log the email sending
				} catch (MessagingException e) { //Log any errors
					LOGGER.log(Level.SEVERE, "Error occurred during ServiceExecution: " + e.getMessage());
				}	
			}
		};
		mailThread.run(); //Execute this thread. It will self-destruct when it is finished
	}
	
	//If the user has set up a recovery email, we will send them a password reset message.
	//If not, then we can ignore this request
	public void sendPasswordResetMessage(String recipient) {
		if (recipient != null) sendMessage(recipient, 
				"Your password has been changed.\n\n"
				+ "If this wasn't you, click the link below to reset your password:\n"
				+ RESET_URL, "Password Changed");
	}
}