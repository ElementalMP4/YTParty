package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class DeleteAccountHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		
		if (tokenService.getUsernameFromToken(token) == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			String username = tokenService.getUsernameFromToken(token);
			tokenService.removeToken(username);
			userService.removeUser(username);
			responder.sendSuccess(session, "Account deleted!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-deleteaccount";
	}

}
