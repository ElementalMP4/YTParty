package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
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
	private GatewayResponseService responder;
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private GatewayAuthService authService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (party.canControlRoom(tokenResponse.getActingString())) {
				if (party.queueIsEmpty()) responder.sendError(session, "The queue is empty! You cannot skip!", this.getHandlerType());
				else {
					party.skipVideo();
					
					ChatMessage skipMessage = new MessageBuilder()
							.partyID(roomID)
							.author(MessageBuilder.SYSTEM_AUTHOR)
							.colour(party.getRoomColour())
							.content(String.format("Video skipped by %s!", tokenResponse.getActingString()))
							.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
							.buildToChatMessage();
					responder.sendChatMessage(party, skipMessage);
				}
			} else responder.sendError(session, "You do not have permission to do that!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-skipvideo";
	}

}
