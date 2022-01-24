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
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class NicknameUpdateHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String nickname = data.getString("nickname").trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		String token = data.getString("token");
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (nickname.equals("") || nickname.length() > 32) responder.sendError(session, "That nickname is too long! It must be less than 32 characters.!", this.getHandlerType());
		else if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String username = tokenService.getUsernameFromToken(token);
			User user = userService.getUser(username);
			user.setNickname(nickname);
			userService.saveUser(user);
			responder.sendSuccess(session, new JSONObject().put("message", "Nickname changed!"), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changenickname";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}