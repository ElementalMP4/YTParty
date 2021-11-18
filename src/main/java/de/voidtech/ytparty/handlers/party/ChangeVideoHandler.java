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
public class ChangeVideoHandler extends AbstractHandler {

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
		String newVideoID = data.getString("video");
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (party.canControlRoom(tokenResponse.getActingString())) {
				responder.sendSuccess(session, new JSONObject().put("video", newVideoID).toString(), this.getHandlerType());
				ChatMessage videoMessage = new MessageBuilder()
						.partyID(roomID)
						.author(MessageBuilder.SYSTEM_AUTHOR)
						.colour(party.getRoomColour())
						.content(String.format("Video Changed by %s!", tokenResponse.getActingString()))
						.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
						.buildToChatMessage();
				party.setVideoID(newVideoID);
				responder.sendChatMessage(party, videoMessage);
				responder.sendSystemMessage(party, new MessageBuilder().type("changevideo").data(new JSONObject().put("video", newVideoID))
						.buildToSystemMessage());	
			} else responder.sendError(session, "You do not have permission to do that!", this.getHandlerType());
		}
	}
	
	@Override
	public String getHandlerType() {
		return "party-changevideo";
	}
}