package main.java.de.voidtech.ytparty.handlers.party;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ChatMessage;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
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
		if (username == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (party == null) responder.sendError(session, "An invalid room ID was provided", this.getHandlerType());
			else {
				responder.sendSuccess(session, new JSONObject()
						.put("video", party.getVideoID())
						.put("canControl", (party.getOwnerName() == null ? true : party.getOwnerName().equals(username)))
						.put("theme", party.getRoomColour())
						.toString(), this.getHandlerType());
				ChatMessage joinMessage = new ChatMessage(roomID, "System", party.getRoomColour(),
						userService.getUser(username).getEffectiveName() + " has joined the party!", "system");
				party.addToSessions(session);
				deliverMessageHistory(session, roomID);
				responder.sendChatMessage(party, joinMessage);
			}
		}
	}

	private void deliverMessageHistory(WebSocketSession session, String roomID) {
		List<ChatMessage> messageHistory = messageService.getMessageHistory(roomID);
		for (ChatMessage message : messageHistory) responder.sendSingleTextMessage(session, message.convertToJSON());
	}

	@Override
	public String getHandlerType() {
		return "party-joinparty";
	}
}