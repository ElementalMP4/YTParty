package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class TypingUpdateHandler extends AbstractHandler {
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private GatewayAuthService authService;
	
	@Autowired
	private PartyService partyService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		String mode = data.getString("mode");
		String user = data.getString("user");
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (!mode.equals("start") && !mode.equals("stop")) 
				responder.sendError(session, "An invalid typing mode was provided", this.getHandlerType());
			else responder.sendSystemMessage(party, new MessageBuilder().type("party-typingupdate").data(new JSONObject()
					.put("mode", mode).put("user", user)).buildToSystemMessage());
		}	
	}

	@Override
	public String getHandlerType() {
		return "party-typingupdate";
	}	
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}