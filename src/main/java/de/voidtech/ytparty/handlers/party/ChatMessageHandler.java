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
public class ChatMessageHandler extends AbstractHandler {
	
	@Autowired
	private GatewayResponseService responder; //We need the responder to respond to requests

	@Autowired
	private GatewayAuthService authService; //We need the auth service to validate room IDs and tokens
	
	@Autowired
	private PartyService partyService; //We need the party service to get a party by ID
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get the user token
		String roomID = data.getString("roomID"); //Get the room ID
		//Get the message content and remove all restricted characters from it to prevent XSS attacks
		String content = data.getString("content").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); 
		String colour = data.getString("colour"); //Get the message colour
		String modifiers = data.getString("modifiers"); //Get the messsage modifiers
		String author = data.getString("author"); //Get the message author
		String avatar = data.getString("avatar"); //Get the author's avatar
		
		//Validate token and room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//If either the room ID or token is invalid, reject them with a message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			if (content.length() > 2000) //If the message content is above 2000 characters, reject it with a message
				responder.sendError(session, "Your message is too long! Messages cannot be longer than 2000 characters.",
						this.getHandlerType());
			else {
				//Build a new chat message with the fields provided by the user
				ChatMessage userMessage = new MessageBuilder()
						.partyID(roomID)
						.content(content)
						.colour(colour)
						.modifiers(modifiers)
						.author(author)
						.avatar(avatar)
						.buildToChatMessage();
				//Broadcast the message to the party
				responder.sendChatMessage(party, userMessage);	
			}
		}		
	}

	@Override
	public String getHandlerType() {
		return "party-chatmessage";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}