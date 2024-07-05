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
public class ChangeVideoHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //We need the responder to respond to requests
	
	@Autowired
	private PartyService partyService; //We need the party service to get a party by ID
	
	@Autowired
	private GatewayAuthService authService; //We need the auth service to validate room IDs and tokens
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get the user's token
		String roomID = data.getString("roomID"); //Get the room ID
		String newVideoID = data.getString("video"); //Get the ID of the new video
		
		//Validate the room ID and token
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If the token or room ID are not valid, reject them with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get the party by ID
			//Check if the user is able to control the room
			if (party.canControlRoom(tokenResponse.getActingString())) {
				//If they are, send a chat message saying the video has been changed, along with a system message to change the video
				ChatMessage videoMessage = new MessageBuilder()
						.partyID(roomID) //Set the room ID of the message
						.author(MessageBuilder.SYSTEM_AUTHOR) //Set the author to System
						.colour(party.getRoomColour()) //Set the message colour
						.content(String.format("Video Changed by %s!", tokenResponse.getActingString())) //Set the content
						.avatar(MessageBuilder.SYSTEM_AVATAR) //Set the avatar
						.buildToChatMessage(); //Build the message
				party.setVideoID(newVideoID); //Change the current video
				responder.sendChatMessage(party, videoMessage); //Send the chat message
				//Send the system message
				responder.sendSystemMessage(party, new MessageBuilder()
						.type("party-changevideo")
						.data(new JSONObject().put("video", newVideoID))
						.buildToSystemMessage());	
			//If the user is not allowed to control the room, send a permissions message
			} else responder.sendError(session, "You're not allowed to change the video!", this.getHandlerType());
		}
	}
	
	@Override
	public String getHandlerType() {
		return "party-changevideo";
	}

	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}