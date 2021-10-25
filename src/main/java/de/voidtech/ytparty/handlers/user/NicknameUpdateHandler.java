package main.java.de.voidtech.ytparty.handlers.user;

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
public class NicknameUpdateHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private AuthService authService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String nickname = data.getString("nickname").trim().replaceAll("<", "\\\\<").replaceAll(">", "\\\\>");
		String token = data.getString("token");
		AuthResponse tokenResponse = authService.validateToken(token); 
		
		if (nickname.equals("") || nickname.length() > 40) responder.sendError(session, "That nickname is not valid!", this.getHandlerType());
		else if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String username = tokenService.getUsernameFromToken(token);
			User user = userService.getUser(username);
			user.setNickname(nickname);
			userService.saveUser(user);
			responder.sendSuccess(session, "Nickname changed!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-changenickname";
	}
}