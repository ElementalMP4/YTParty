package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class ChangeAvatarHandler extends AbstractHandler {

	@Autowired
	private GatewayAuthService authService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token");
		String avatar = data.getString("avatar");
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String username = tokenResponse.getActingString();
			User user = userService.getUser(username);
			user.setProfilePicture(avatar);
			userService.saveUser(user);
			responder.sendSuccess(session, new JSONObject().put("message", "Avatar changed!"), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changeavatar";
	}

	@Override
	public boolean requiresRateLimit() {
		return true;
	}

}
