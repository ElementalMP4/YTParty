package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class TypingUpdateHandler extends AbstractHandler {
	
	@Autowired
	private GatewayResponseService responder; //Responds to requests

	@Autowired
	private GatewayAuthService authService; //Validates tokens and room IDs
	
	@Autowired
	private PartyService partyService; //Gets party by ID
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Gets token
		String roomID = data.getString("roomID"); //Gets room ID
		String mode = data.getString("mode"); //Gets typing mode (start/stop)
		String user = data.getString("user"); //Gets user whos is typing
		
		//Validate token and room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//Reject if invalid with message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			if (!mode.equals("start") && !mode.equals("stop"))  //Check typing mode. Reject if mode is invalid
				responder.sendError(session, "An invalid typing mode was provided", this.getHandlerType());
			//Otherwise send typing system message
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