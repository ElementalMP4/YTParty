package main.java.de.voidtech.ytparty.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.AuthResponse;

@Service
public class GatewayAuthService {

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private PartyService partyService;
	
	public AuthResponse validateToken(String token) { 
		AuthResponse response = new AuthResponse();
		String username = tokenService.getUsernameFromToken(token);
		if (username == null) {
			response.setMessage("An invalid token was provided");
			response.setSuccess(false);
		} else response.setSuccess(true);
		response.setActingString(username);
		return response;
	}
	
	public AuthResponse validatePartyID(String roomID) {
		AuthResponse response = new AuthResponse();
		if (partyService.getParty(roomID) == null) {
			response.setMessage("An invalid room ID was provided");
			response.setSuccess(false);
		} else response.setSuccess(true);
		response.setActingString(roomID);
		return response;
	}
}