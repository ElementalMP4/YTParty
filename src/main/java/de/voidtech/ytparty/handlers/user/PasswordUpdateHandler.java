package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MailService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class PasswordUpdateHandler extends AbstractHandler {

	@Autowired
	private UserTokenService tokenService; //Used to reset a user's token after they change their password
	
	@Autowired
	private GatewayResponseService responder; //Used to reply to the user
	
	@Autowired
	private UserService userService; //Used to retrieve and modify user data
	
	@Autowired
	private GatewayAuthService authService; //Used to authenticate the user's token
	
	@Autowired
	private MailService mailService; //Used to send an email to the user notifying them that their password has been changed
	
	//Used to validate the password complexity (8+ characters, a number, a lowercase letter and an uppercase letter)
	private Pattern passwordPattern = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token =  data.getString("token"); //Get the user's token
		String currentPassword = data.getString("original-password").trim(); //Get the original password
		String newPassword = data.getString("new-password").trim(); //Get the new password
		String newPasswordConfirm = data.getString("password-match").trim(); //Get the second entry of the new password
		
		AuthResponse tokenResponse = authService.validateToken(token); //Validate the token
		
		//If the token is invalid, reject it with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			//Otherwise get the user from their username
			String username = tokenResponse.getActingString();
			User user = userService.getUser(username);
			//Validate their current password
			if (!user.checkPassword(currentPassword)) 
				responder.sendError(session, "Your current password is not correct!", this.getHandlerType());
			//Compare the new password entries
			else if (!newPassword.equals(newPasswordConfirm))
				responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
			//Validate the new password
			else if (!passwordPattern.matcher(newPassword).matches())
				responder.sendError(session, "That password is not valid! Make sure it contains a capital letter and"
						+ " a number and is at least 8 characters!", this.getHandlerType());
			else {
				//Change the password and send a success message
				user.setPassword(newPassword);
				userService.saveUser(user);
				tokenService.removeToken(username);
				String newToken = tokenService.getToken(username);
				responder.sendSuccess(session, new JSONObject().put("token", newToken), this.getHandlerType());
				mailService.sendPasswordResetMessage(user.getEmail());
			}
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changepassword";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}