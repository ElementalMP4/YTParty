package main.java.de.voidtech.ytparty.handlers.user;

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
public class SignInHandler extends AbstractHandler {
	
	@Autowired
	private UserService userService; //User service is needed to get the user object for a username
	
	@Autowired
	private GatewayResponseService responder; //Responder is needed to reply to the request

	@Autowired
	private UserTokenService tokenService; //Token service is needed to get the token of a user 
	
	@Autowired
	private CaptchaAuthService captchaService; //Captcha service is needed to authenticate recaptcha token
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String username = data.getString("username"); //Get the entered username
		String enteredPassword = data.getString("password"); //Get the entered password
		String captchaToken = data.getString("captcha-token"); //Get the captcha token
		User user = userService.getUser(username); //Retrieve the user with the given username
		
		if (captchaService.validateCaptcha(captchaToken)) { //Check the captcha token first to prevent automated username checking
			if (user == null) responder.sendError(session, "Username or Password incorrect", this.getHandlerType()); 
			//Check if user exists, if not then return an error
			else {
				if (user.checkPassword(enteredPassword)) 
					responder.sendSuccess(session, new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
					//Check password, if correct then send token to the user
				else
					responder.sendError(session, "Username or Password incorrect", this.getHandlerType());
					//Otherwise report error to user
			}	
		} else
			responder.sendError(session, "You need to pass the captcha!", this.getHandlerType());
			//If captcha token is not valid or not present, report error to user
	}

	@Override
	public String getHandlerType() {
		return "user-signin";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}