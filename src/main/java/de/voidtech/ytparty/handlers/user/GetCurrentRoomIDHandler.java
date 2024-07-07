package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.SessionService;

@Handler
public class GetCurrentRoomIDHandler extends AbstractHandler {

	@Autowired
	private GatewayAuthService authService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private SessionService sessionService;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token");
		AuthResponse tokenResponse = authService.validateToken(token);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String roomID = sessionService.getSessionRoomIDifExists(tokenResponse.getActingString());
			roomID = roomID == null ? "none" : roomID;
			responder.sendSuccess(session, new JSONObject().put("roomID", roomID), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-getroom";
	}

	@Override
	public boolean requiresRateLimit() {
		return false;
	}

}
