package main.java.de.voidtech.ytparty.handlers.user;

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
public class SignInHandler extends AbstractHandler {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String username = data.getString("username");
		String enteredPassword = data.getString("password");
		User user = userService.getUser(username);
		
		if (user == null) responder.sendError(session, "No user found with that name!", this.getHandlerType());
		else {
			if (user.checkPassword(enteredPassword)) responder.sendSuccess(session, tokenService.getToken(username), this.getHandlerType());
			else responder.sendError(session, "Invalid Password", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-signin";
	}
}