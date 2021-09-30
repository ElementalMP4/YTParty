package main.java.de.voidtech.ytparty.handlers.party;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.AuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class GetQueueHandler extends AbstractHandler {
	
	private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

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
		
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		else {
			Party party = partyService.getParty(roomID);
			List<String> videos = new ArrayList<String>(party.getQueue());
			String videoList = "Video Queue:<br><br>";
			
			for (String video : videos) {
				videoList = videoList + String.format("<a href='%s'>%s</a><br>", YOUTUBE_BASE_URL + video, video);
			}
			
			responder.sendSingleTextMessage(session,
					new ChatMessage(party.getPartyID(), "System", "#FF0000", videoList, "system").convertToJSON());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-getqueue";
	}
}