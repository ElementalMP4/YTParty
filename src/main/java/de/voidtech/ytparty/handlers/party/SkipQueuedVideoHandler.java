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
public class SkipQueuedVideoHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //Responds to requests
	
	@Autowired
	private PartyService partyService; //Gets party by ID
	
	@Autowired
	private GatewayAuthService authService; //Validates tokens and room IDs
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get token
		String roomID = data.getString("roomID"); //Get room ID
		
		//Validate token & room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//Reject if invalid with message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			if (party.canControlRoom(tokenResponse.getActingString())) { //Determine whether user can control room
				if (party.queueIsEmpty()) //Send error if queue is empty
					responder.sendError(session, "The queue is empty! You cannot skip!", this.getHandlerType());
				else {
					//Otherwise skip video and send message saying who skipped the video
					party.skipVideo();
					
					ChatMessage skipMessage = new MessageBuilder()
							.partyID(roomID)
							.author(MessageBuilder.SYSTEM_AUTHOR)
							.colour(party.getRoomColour())
							.content(String.format("Video skipped by %s!", tokenResponse.getActingString()))
							.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
							.avatar(MessageBuilder.SYSTEM_AVATAR)
							.buildToChatMessage();
					responder.sendChatMessage(party, skipMessage);
				}
			//Send permissions message if user cannot skip videos
			} else responder.sendError(session, "You're not allowed to skip videos", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-skipvideo";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}

}
