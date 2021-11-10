package main.java.de.voidtech.ytparty.handlers.party;

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
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class ChatMessageHandler extends AbstractHandler {
	
	@Autowired
	private GatewayResponseService responder;

	@Autowired
	private AuthService authService;
	
	@Autowired
	private PartyService partyService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		String content = data.getString("content").replaceAll("<", "").replaceAll(">", "");
		String colour = data.getString("colour");
		String modifiers = data.getString("modifiers");
		String author = data.getString("author");
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (content.length() > 800) responder.sendError(session, "Your message is too long! Messages cannot be longer than 800 characters.", this.getHandlerType());
			else {
				ChatMessage userMessage = new MessageBuilder()
						.partyID(roomID)
						.content(content)
						.colour(colour)
						.modifiers(modifiers)
						.author(author)
						.buildToChatMessage();
				responder.sendChatMessage(party, userMessage);	
			}
		}		
	}

	@Override
	public String getHandlerType() {
		return "party-chatmessage";
	}
}