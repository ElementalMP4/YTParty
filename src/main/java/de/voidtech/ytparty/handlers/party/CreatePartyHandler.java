package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class CreatePartyHandler extends AbstractHandler{

	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private PartyService partyService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		boolean ownerControlsOnly = data.getBoolean("ownerControlsOnly");
		String videoID = data.getString("videoID");
		String roomThemeColour = data.getString("theme");
		
		AuthResponse tokenResponse = authService.validateToken(token);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String ownerUsername = tokenService.getUsernameFromToken(token);
			Party party = new Party(partyService.generateRoomID(), ownerUsername, roomThemeColour, videoID, ownerControlsOnly);
			partyService.saveParty(party);
			responder.sendSuccess(session, new JSONObject().put("partyID", party.getPartyID()), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-createparty";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}