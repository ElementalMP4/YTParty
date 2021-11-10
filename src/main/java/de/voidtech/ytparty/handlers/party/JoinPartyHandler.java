package main.java.de.voidtech.ytparty.handlers.party;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.message.ChatMessage;
import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.AuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MessageService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class JoinPartyHandler extends AbstractHandler{

	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private AuthService authService;
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageService messageService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		String username = tokenService.getUsernameFromToken(token); 
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
			else {
				Party party = partyService.getParty(roomID);
				responder.sendSuccess(session, new JSONObject()
						.put("video", party.getVideoID())
						.put("canControl", (party.getOwnerName() == null ? true : party.getOwnerName().equals(username)))
						.put("theme", party.getRoomColour())
						.toString(), this.getHandlerType());
				
				ChatMessage joinMessage = new MessageBuilder()
						.partyID(roomID)
						.author(MessageBuilder.SYSTEM_AUTHOR)
						.colour(party.getRoomColour())
						.content(String.format("%s has joined the party!", userService.getUser(username).getEffectiveName()))
						.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
						.buildToChatMessage();
				party.addToSessions(session);
				deliverMessageHistory(session, roomID);
				responder.sendChatMessage(party, joinMessage);
			}
		}

	private void deliverMessageHistory(WebSocketSession session, String roomID) {
		List<ChatMessage> messageHistory = messageService.getMessageHistory(roomID);
		for (ChatMessage message : messageHistory) responder.sendSingleMessage(session, message);
	}

	@Override
	public String getHandlerType() {
		return "party-joinparty";
	}
}