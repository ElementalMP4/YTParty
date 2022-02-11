package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.PasswordResetCase;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MailService;
import main.java.de.voidtech.ytparty.service.PasswordResetService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class PasswordForgottenHandler extends AbstractHandler {
	
	//URL that the user will click in an email to reset their password
	private static final String RESET_URL = "https://ytparty.voidtech.de/resetpassword.html";

	@Autowired
	private GatewayResponseService responder; //Send replies to the user
	
	@Autowired
	private MailService mailService; //Send an email to the user
	
	@Autowired
	private UserService userService; //Get and modify user information
	
	@Autowired
	private PasswordResetService passwordService; //Timed reset case manager
	
	@Autowired
	private CaptchaAuthService captchaService; //Google Captcha Validator service
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String username = data.getString("username"); //Get the username
		String captchaToken = data.getString("captcha-token"); //Get the captcha token
		User user = userService.getUser(username); //Get the user from the username
		
		if (!captchaService.validateCaptcha(captchaToken)) //Validate the captcha
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else if (user == null) //Ensure the user exists
			responder.sendError(session, "That username does not exist!", this.getHandlerType());
		else { //If the user has an email associated with their account, send the email
			String email = user.getEmail();
			if (email == null) responder.sendError(session, "This account has no recovery email!", this.getHandlerType());
			else {
				String title = "Password reset request";
				PasswordResetCase resetCase = passwordService.openPasswordResetCase(username); //Create a new timed password reset case
				String body = "You have requested to reset your password. Please click the link below to continue:\n\n" +
				RESET_URL + "?token=" + resetCase.getToken() + "&user=" + username +
					"\n\nKeep this link a secret! Someone could use it to take over your account.";
				
				mailService.sendMessage(email, body, title); //Send the email
				
				responder.sendSuccess(session, new JSONObject().put("message",
						"Please check your e-mail inbox. Please also check your spam if you cannot find our message."),
						this.getHandlerType());	
			}
		}
	}

	@Override
	public String getHandlerType() {
		return "user-forgottenpassword";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}

}
