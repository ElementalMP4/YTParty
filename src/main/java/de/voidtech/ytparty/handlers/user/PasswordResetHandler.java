package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MailService;
import main.java.de.voidtech.ytparty.service.PasswordResetService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class PasswordResetHandler extends AbstractHandler {
	
	//We need the password validation regex
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Autowired
	private GatewayResponseService responder; //We need the responder to send data back to the user
	
	@Autowired
	private CaptchaAuthService captchaService; //We need the captcha service to validate the captcha
	
	@Autowired
	private UserService userService; //The user service is used to persist the new password

	@Autowired
	private PasswordResetService passwordService; //The password service is used to validate the reset token
	
	@Autowired
	private MailService mailService; //The mail service is used to notify the user that their password has been reset
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String resetToken = data.getString("reset-token"); //Get the reset token
		String password = data.getString("password"); //Get the password
		String passwordRepeat = data.getString("password-confirm");	//Get the password confirm value
		String captchaToken = data.getString("captcha-token"); //Get the captcha token
		String username = data.getString("username"); //Get the username
		
		//Validate the reset token
		if (passwordService.getCaseFromResetToken(resetToken) == null)
			responder.sendError(session, "This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
		//Ensure that the reste token is for the username provided
		else if (!passwordService.getCaseFromResetToken(resetToken).getUser().equals(username))
			responder.sendError(session, "This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
		//Compare the entered passwords
		if (!password.equals(passwordRepeat))
			responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
		//Validate the password
		else if (!PASSWORD_PATTERN.matcher(password).matches())
			responder.sendError(session, "The password you entered does not meet the complexity requirements! "
					+ "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
		//Validate the captcha token
		else if (!captchaService.validateCaptcha(captchaToken))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else {
			//Update the password and notify the user
			User user = userService.getUser(username);
			user.setPassword(password);
			userService.saveUser(user);
			passwordService.closePasswordCase(passwordService.getCaseFromResetToken(resetToken));
			responder.sendSuccess(session, new JSONObject().put("message", "Password reset successfully!"), this.getHandlerType());
			mailService.sendPasswordResetMessage(user.getEmail());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-resetpassword";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}

}
