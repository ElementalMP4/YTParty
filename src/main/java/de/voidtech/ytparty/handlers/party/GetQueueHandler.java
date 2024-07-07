package main.java.de.voidtech.ytparty.handlers.party;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;

@Handler
public class GetQueueHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //Responds to requests
	
	@Autowired
	private GatewayAuthService authService; //Validates tokens and room IDs
	
	@Autowired
	private PartyService partyService; //Gets parties by ID
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get token
		String roomID = data.getString("roomID"); //Get room ID
		
		//Validate token and room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//Reject token or room ID of they are not valid
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			List<String> videos = new ArrayList<String>(party.getQueueAsList()); //Create new list out of the video queue
			//Convert list to a JSON array and send it to the client to be processed
			responder.sendSuccess(session, new JSONObject().put("videos", videos.toArray()), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "party-getqueue";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}
}