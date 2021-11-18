package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class GetProfileHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else{
			User user = userService.getUser(tokenService.getUsernameFromToken(token));
			String userData = new JSONObject()
					.put("nickname", user.getNickname())
					.put("colour", user.getHexColour())
					.put("effectiveName", user.getEffectiveName())
					.put("username", user.getUsername())
				.toString();
			responder.sendSuccess(session, userData, this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-getprofile";
	}
}