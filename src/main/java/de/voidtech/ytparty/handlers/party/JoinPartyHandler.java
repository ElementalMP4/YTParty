package main.java.de.voidtech.ytparty.handlers.party;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.ChatMessageService;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class JoinPartyHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //responds to requests from the user
	
	@Autowired
	private GatewayAuthService authService; //Validates tokens and room IDs
	
	@Autowired
	private PartyService partyService; //Gets party information by party ID
	
	@Autowired
	private UserService userService; //Gets a user object
	
	@Autowired
	private ChatMessageService messageService; //Gets the room message history
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get user token
		String roomID = data.getString("roomID"); //Get room ID
		
		//Validate token & room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//Reject if invalid with message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			String username = tokenResponse.getActingString(); //Get username of user
			User user = userService.getUser(username); //Get user from their username
			Party party = partyService.getParty(roomID); //Get party from ID
			session.setName(user.getEffectiveName()); //Get effective name of user (we use this to identify this user when they leave)
			responder.sendSuccess(session, new JSONObject() //Send room data to user
					.put("video", party.getVideoID())
					.put("canControl", party.canControlRoom(username))
					.put("theme", party.getRoomColour())
					.put("owner", party.getOwnerName()), this.getHandlerType());
			
			ChatMessage joinMessage = new MessageBuilder() //Create new message saying this user has joined
					.partyID(roomID)
					.author(MessageBuilder.SYSTEM_AUTHOR)
					.colour(party.getRoomColour())
					.content(String.format("%s has joined the party!", user.getEffectiveName()))
					.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
					.avatar(MessageBuilder.SYSTEM_AVATAR)
					.buildToChatMessage();
			party.addToSessions(session); //Add user to room
			deliverMessageHistory(session, roomID); //Send history
			responder.sendChatMessage(party, joinMessage); //Send join message
		}
	}

	private void deliverMessageHistory(GatewayConnection session, String roomID) {
		List<ChatMessage> messageHistory = messageService.getMessageHistory(roomID); //Get history from message service
		responder.sendChatHistory(session, messageHistory); //Send history to session
	}

	@Override
	public String getHandlerType() {
		return "party-joinparty";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}