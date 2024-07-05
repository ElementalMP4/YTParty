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
public class GetProfileHandler extends AbstractHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder; //We need the responder to reply to the user
	
	@Autowired
	private GatewayAuthService authService; //We need the auth service to authenticate the user's token
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get the token
		
		AuthResponse tokenResponse = authService.validateToken(token); //Validate the token
		
		//If the token is invalid, reject it with a message.
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			//Otherwise, we can send the user their data:
			String username = tokenResponse.getActingString(); //Get the username of the user
			User user = userService.getUser(username); //Get the user's data from the DB
			JSONObject userData = new JSONObject()
					.put("nickname", user.getNickname()) //Nickname
					.put("colour", user.getHexColour()) //Nickname colour
					.put("effectiveName", user.getEffectiveName()) //Display name : if there is no nickname, show username
					.put("avatar", user.getProfilePicture()) //Avatar choice
					.put("username", user.getUsername()); //Username
			responder.sendSuccess(session, userData, this.getHandlerType()); //Send this data in a success message to the user
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