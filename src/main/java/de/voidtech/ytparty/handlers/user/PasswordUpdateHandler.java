package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.AuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
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
	private AuthService authService;
	
	private Pattern passwordPattern = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String username = tokenService.getUsernameFromToken(data.getString("token"));
		String token =  data.getString("token");
		String password = data.getString("password").trim();
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!passwordPattern.matcher(password).matches()) responder.sendError(session, "That password is not valid! Make sure it contains a capital letter and a number and is at least 8 characters!", this.getHandlerType());
		else {
			User user = userService.getUser(username);
			user.setPassword(password);
			userService.saveUser(user);
			tokenService.removeToken(username);
			responder.sendSuccess(session, "Password changed", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changepassword";
	}
}