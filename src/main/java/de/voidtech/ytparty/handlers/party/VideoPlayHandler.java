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
public class VideoPlayHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //Inject the responder so we can respond to requests
	
	@Autowired
	private GatewayAuthService authService; //We need the auth service to validate tokens and party IDs
	
	@Autowired
	private PartyService partyService; //We need the party service to locate the party to control
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //We need the user's token
		String roomID = data.getString("roomID"); //We need the room ID
		int timestamp = data.getInt("timestamp"); //We need the timestamp to start playing from
		
		//Validate the room ID and the token
		AuthResponse tokenResponse = authService.validateToken(token);
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If either the token or room ID is invalid, we can reject them with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise...
		else {
			Party party = partyService.getParty(roomID); //Get the room by its ID
			if (party.canControlRoom(tokenResponse.getActingString())) //Check that this person can control the room
				//If they can, send a system message to start playing the video
				responder.sendSystemMessage(party, new MessageBuilder()
						.type("party-playvideo")
						.data(new JSONObject().put("time", timestamp))
						.buildToSystemMessage());
			//If not, send a permissions error message
			else responder.sendError(session, "You're not allowed to start the video!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-playvideo";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}