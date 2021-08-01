package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class SignupHandler extends AbstractHandler {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserTokenService tokenService;
	
	private Pattern passwordPattern = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String username = data.getString("username").trim();
		String password = data.getString("password").trim();
		
		if (username == "") responder.sendError(session, "That username is not valid!", this.getHandlerType());
		else if (password == "") responder.sendError(session, "That password is not valid!", this.getHandlerType());
		else if (!passwordPattern.matcher(password).matches()) responder.sendError(session, "That password is not valid!", this.getHandlerType());
		else if (userService.usernameInUse(username)) responder.sendError(session, "That username is already in use!", this.getHandlerType());
		else {
			User newUser = new User(username, "", password, "#FF0000");
			userService.saveUser(newUser);
			responder.sendSuccess(session, tokenService.getToken(username), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-signup";
	}
}