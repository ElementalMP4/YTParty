package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import main.java.de.voidtech.ytparty.persistence.ChatMessage;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class ClearQueueHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //We need the responder to respond to requests
	
	@Autowired
	private PartyService partyService; //We need the party service to get rooms by ID
	
	@Autowired
	private GatewayAuthService authService; //We need the auth service to validate tokens and room IDs

	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get the token
		String roomID = data.getString("roomID"); //Get the room ID
		
		//Validate token and room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If the token or room ID is invalid, reject them with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			if (party.canControlRoom(tokenResponse.getActingString())) { //Check if user can control room
				party.clearQueue(); //Clear queue
				ChatMessage clearMessage = new MessageBuilder()
						.partyID(roomID)
						.author(MessageBuilder.SYSTEM_AUTHOR)
						.colour(party.getRoomColour())
						.content(String.format("Queue cleared by %s!", tokenResponse.getActingString()))
						.avatar(MessageBuilder.SYSTEM_AVATAR)
						.buildToChatMessage(); //Build a new chat message saying who cleared the queue
				responder.sendChatMessage(party, clearMessage); //Send the chat message
			//If the user cannot control the room, send a permissions message
			} else responder.sendError(session, "You're not allowed to clear the queue!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-clearqueue";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}