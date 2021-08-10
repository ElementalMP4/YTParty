package main.java.de.voidtech.ytparty.handlers.party;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ChatMessage;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.entities.SystemMessage;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class ChangeVideoHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private PartyService partyService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String token = data.getString("token");
		String roomID = data.getString("roomID");
		String newVideoID = data.getString("video");
		
		String username = tokenService.getUsernameFromToken(token); 
		if (username == null) responder.sendError(session, "An invalid token was provided", this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			if (party == null) responder.sendError(session, "An invalid room ID was provided", this.getHandlerType());
			else {
				responder.sendSuccess(session, new JSONObject().put("video", newVideoID).toString(), this.getHandlerType());
				ChatMessage videoMessage = new ChatMessage(roomID, "System", "#ff0000", "The video has been changed!", "system");
				party.setVideoID(newVideoID);
				responder.sendChatMessage(party, videoMessage);
				responder.sendSystemMessage(party, new SystemMessage("changevideo", new JSONObject().put("video", newVideoID)));
			}
		}
	}

	@Override
	public String getHandlerType() {
		return "party-changevideo";
	}

}
