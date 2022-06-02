package main.java.de.voidtech.ytparty.handlers.party;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MessageHandler;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class CreatePartyHandler extends AbstractHandler{

	@Autowired
	private GatewayResponseService responder; //Responder is needed to respond to requests
	
	@Autowired
	private GatewayAuthService authService; //Auth service validates tokens
	
	@Autowired
	private UserTokenService tokenService; //Token service gets usernames from tokens
	
	@Autowired
	private PartyService partyService; //Party service stores new parties

	private static final Logger LOGGER = Logger.getLogger(CreatePartyHandler.class.getName());
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String token = data.getString("token"); //Get token
		boolean ownerControlsOnly = data.getBoolean("ownerControlsOnly"); //Get room permission settings
		String videoID = data.getString("videoID"); //Get initial video ID
		String roomThemeColour = data.getString("theme"); //Get room colour
		
		//Validate token
		AuthResponse tokenResponse = authService.validateToken(token);
		
		//If token is invalid, reject it
		if (!tokenResponse.isSuccessful()) responder.sendError(session, tokenResponse.getMessage(), this.getHandlerType());
		else {
			String ownerUsername = tokenService.getUsernameFromToken(token); //Get owner name
			//Create new party
			Party party = new Party(partyService.generateRoomID(), ownerUsername, roomThemeColour, videoID, ownerControlsOnly);
			partyService.saveParty(party); //Save party
			//Reply with room ID
			responder.sendSuccess(session, new JSONObject().put("partyID", party.getPartyID()), this.getHandlerType()); 
			LOGGER.log(Level.INFO, "Party created by " + ownerUsername);
		}
	}

	@Override
	public String getHandlerType() {
		return "party-createparty";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}
}