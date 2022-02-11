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
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class SignupHandler extends AbstractHandler {
	
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	//Password must be a minimum of 8 characters, must contain one number, must contain both lowercase and uppercase characters.

	@Autowired
	private UserTokenService tokenService; //Required to make a new token for the new user
	
	@Autowired
	private UserService userService; //Required to persist the user to the database
	
	@Autowired
	private GatewayResponseService responder; //Required to send responses to the user
	
	@Autowired
	private CaptchaAuthService captchaService; //Required to authenticate the captcha token
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String username = data.getString("username"); //Get username 
		String password = data.getString("password"); //Get password
		String passwordConfirm = data.getString("password-confirm"); //Get the second password entry
		String avatar = data.getString("avatar"); //Get the avatar selection
		String captchaToken = data.getString("captcha-token"); //Get the recaptcha token
		String email = data.getString("email"); //Get the email (optional)
		
		//Check username is valid
		if (username.equals(""))
			responder.sendError(session, "That username is not valid!", this.getHandlerType());
		else if (username.length() > 32)
			responder.sendError(session, "That username is too long! It must be less than 32 characters.", this.getHandlerType());
		//Check password is valid
		else if (!password.equals(passwordConfirm))
			responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
		else if (!PASSWORD_PATTERN.matcher(password).matches())
			responder.sendError(session, "The password you entered does not meet the complexity requirements! "
					+ "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
		//Check username is not in use
		else if (userService.usernameInUse(username))
			responder.sendError(session, "That username is already in use!", this.getHandlerType());
		//Check for avatar
		else if (avatar.equals(""))
			responder.sendError(session, "You must choose an avatar!", this.getHandlerType());
		//Check captcha token
		else if (!captchaService.validateCaptcha(captchaToken))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		//Create user
		else {
			User newUser = new User(username, null, password, "#FF0000",
					(email.equals("") ? null : email), avatar); //Create a new user with the parameters provided
			userService.saveUser(newUser); //Save the new user
			responder.sendSuccess(session, new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
			//Send the user their authentication token
		}
	}

	@Override
	public String getHandlerType() {
		return "user-signup";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}

}
