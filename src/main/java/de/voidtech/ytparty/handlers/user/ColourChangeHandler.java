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
public class ColourChangeHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String colour = data.getString("colour");
		String token = data.getString("token");
		
		if (tokenService.getUsernameFromToken(token) == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			String username = tokenService.getUsernameFromToken(token);
			User user = userService.getUser(username);
			user.setHexColour(colour);
			userService.saveUser(user);
			responder.sendSuccess(session, "Colour changed!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changecolour";
	}
}