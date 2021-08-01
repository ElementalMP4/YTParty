package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class CreatePartyHandler extends AbstractHandler{

	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private PartyService partyService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		boolean roomHasOwner = data.getBoolean("roomHasOwner");
		String videoID = data.getString("videoID");
		
		if (tokenService.getUsernameFromToken(token) == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			String ownerUsername = null;
			if (roomHasOwner) ownerUsername = tokenService.getUsernameFromToken(token);
			Party party = new Party(partyService.generateRoomID(), ownerUsername, videoID);
			partyService.saveParty(party);
			responder.sendSuccess(session, party.getPartyID(), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-createparty";
	}
}