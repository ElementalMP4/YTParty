package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class VideoEndHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder; //Responds to requests
	
	@Autowired
	private PartyService partyService; //Gets party by ID
	
	@Autowired
	private GatewayAuthService authService; //Validates tokens and room IDs
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get token
		String roomID = data.getString("roomID"); //Get room ID
		
		//Validate token & room ID
		AuthResponse tokenResponse = authService.validateToken(token); 
		AuthResponse partyIDResponse = authService.validatePartyID(roomID);
		
		//Reject if token or room ID is invalid with message
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else if (!partyIDResponse.isSuccessful()) responder.sendError(session, partyIDResponse.getMessage(), this.getHandlerType());
		//Otherwise
		else {
			Party party = partyService.getParty(roomID); //Get party by ID
			party.incrementFinishedCount();
			//Add this person to the finished players count. This will wait for everyone to finish before cueing the next video
		}
	}

	@Override
	public String getHandlerType() {
		return "party-videoend";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return false;
	}

}
