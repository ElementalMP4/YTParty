package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
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
	private UserTokenService tokenService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Autowired
	private MailService mailService;
	
	private Pattern passwordPattern = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String username = tokenService.getUsernameFromToken(data.getString("token"));
		String token =  data.getString("token");

		String currentPassword = data.getString("original-password").trim();
		String newPassword = data.getString("new-password").trim();
		String newPasswordConfirm = data.getString("password-match").trim();
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			User user = userService.getUser(username);
			if (!user.checkPassword(currentPassword)) responder.sendError(session, "Your current password is not correct!", this.getHandlerType());
			else if (!newPassword.equals(newPasswordConfirm)) responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
			else if (!passwordPattern.matcher(newPassword).matches()) responder.sendError(session, "That password is not valid! Make sure it contains a capital letter and a number and is at least 8 characters!", this.getHandlerType());
			else {
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