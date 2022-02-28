package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class EnqueueVideoHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //Responder responds to requests
	
	@Autowired
	private PartyService partyService; //Party service gets party by ID
	
	@Autowired
	private GatewayAuthService authService; //Auth service validates token and room ID
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get token
		String roomID = data.getString("roomID"); //Get room ID
		String newVideoID = data.getString("video"); //Get video to be queued
		
		//Validate token & room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If token or room ID is invalid, reject with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party
			if (party.canControlRoom(tokenResponse.getActingString())) { //Determine whether user can control room
				ChatMessage videoMessage = new MessageBuilder()
						.partyID(roomID)
						.author(MessageBuilder.SYSTEM_AUTHOR)
						.colour(party.getRoomColour())
						.content(String.format("Video queued by %s!", tokenResponse.getActingString()))
						.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
						.avatar(MessageBuilder.SYSTEM_AVATAR)
						.buildToChatMessage(); //Create new message saying video has been queued by user
				party.enqueueVideo(newVideoID); //Queue the video
				responder.sendChatMessage(party, videoMessage); //Send the chat message
			//If user does not have permission, send a permission message
			} else responder.sendError(session, "You do not have permission to do that!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-queuevideo";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}