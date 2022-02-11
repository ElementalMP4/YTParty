package main.java.de.voidtech.ytparty.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;

@Service
public class GatewayAuthService {

	@Autowired
	private UserTokenService tokenService; //Inject the TokenService to validate tokens
	
	@Autowired
	private PartyService partyService; //Inject the PartyService to validate room IDs
	
	public AuthResponse validateToken(String token) { //This method validates user tokens
		AuthResponse response = new AuthResponse(); //Create a new response container
		String username = tokenService.getUsernameFromToken(token); //Use the tokenService to validate a token
		if (username == null) { //If the token is valid, set the response message and set the success to false.
			response.setMessage("An invalid token was provided");
			response.setSuccess(false);
		} else response.setSuccess(true); //Otherwise, set the success to true.
		response.setActingString(username);
		//Set the acting string to the username we retrieved so we can use it again later, reducing DB load
		return response;
	}
	
	//This method is identical to the previous method, however it validates room IDs instead of tokens.
	public AuthResponse validatePartyID(String roomID) { 
		AuthResponse response = new AuthResponse();
		if (partyService.getParty(roomID) == null) { //Use the party service instead of the token service for validation
			response.setMessage("An invalid room ID was provided"); //Set an appropriate message
			response.setSuccess(false);
		} else response.setSuccess(true);
		response.setActingString(roomID);
		return response;
	}
}