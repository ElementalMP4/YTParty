package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

public class GetPartyHandler extends AbstractHandler {
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private PartyService partyService;

	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		String username = tokenService.getUsernameFromToken(token); 
		if (username == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (party == null) responder.sendError(session, "An invalid room ID was provided", this.getHandlerType());
			else responder.sendSuccess(session, new JSONObject().put("video", party.getVideoID()).toString(), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-getvideo";
	}
}