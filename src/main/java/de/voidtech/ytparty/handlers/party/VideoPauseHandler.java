package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class VideoPauseHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //We need the responder to respond to requests
	
	@Autowired
	private GatewayAuthService authService; //We need the auth service to validate room IDs and tokens
	
	@Autowired
	private PartyService partyService; //We need the party service to get rooms by their ID
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get the user's token
		String roomID = data.getString("roomID"); //Get the room ID
		
		//Validate the room ID and token
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If either response is not successful, reject them with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get the party by its ID
			if (party.canControlRoom(tokenResponse.getActingString())) //If the user can control the room...
				//Send a system message to pause the player in all clients
				responder.sendSystemMessage(party, new MessageBuilder()
						.type("party-pausevideo")
						.data(MessageBuilder.EMPTY_JSON)
						.buildToSystemMessage());
			//If not, send a permissions message
			else responder.sendError(session, "You're not allowed to pause the video!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-pausevideo";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}