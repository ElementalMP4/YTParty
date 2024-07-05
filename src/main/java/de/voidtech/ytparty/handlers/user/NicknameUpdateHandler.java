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
public class NicknameUpdateHandler extends AbstractHandler {

	@Autowired
	private UserService userService; //User service is needed to modify data
	
	@Autowired
	private GatewayResponseService responder; //Responder is needed to reply to the user
	
	@Autowired
	private GatewayAuthService authService; //Auth service is used to verify tokens
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String nickname = data.getString("nickname").trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;"); 
		//Get the input nickname, make sure it isn't just spaces, and filter out XSS
		String token = data.getString("token"); //Get the user's token
		AuthResponse tokenResponse = authService.validateToken(token); //Validate the user token
		
		//If the name is too long, reject it
		if (nickname.equals("") || nickname.length() > 32) responder.sendError(session,
				"That nickname is too long! It must be less than 32 characters.!", this.getHandlerType());
		//If the user token is not valid, reject it
		else if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			//Otherwise, get the user, modify their name and persist the changed user.
			String username = tokenResponse.getActingString(); //Get the username from the token response
			User user = userService.getUser(username); //Get the user
			user.setNickname(nickname); //Set their nickname
			userService.saveUser(user); //Persist to the DB
			//Send a success message
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