package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class GetProfileHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token");
		
		AuthResponse tokenResponse = authService.validateToken(token);

		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String username = tokenResponse.getActingString();
			User user = userService.getUser(username);
			JSONObject userData = new JSONObject()
					.put("nickname", user.getNickname())
					.put("colour", user.getHexColour())
					.put("effectiveName", user.getEffectiveName())
					.put("avatar", user.getProfilePicture())
					.put("username", user.getUsername());
			responder.sendSuccess(session, userData, this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-getprofile";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}