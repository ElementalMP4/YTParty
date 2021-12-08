package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.time.Instant;

public class Session {	
	private int requestAllowance;
	private long lastInteraction;
	private boolean connectionBlocked;
	
	private static final int EXPIRY_TIME_SECONDS = 900; 
	private static final int MAX_REQUEST_ALLOWANCE = 20;
	
	private void updateLastInteraction() {
		lastInteraction = Instant.now().getEpochSecond();
	}
	
	public Session() {
		connectionBlocked = false;
		requestAllowance = MAX_REQUEST_ALLOWANCE;
		updateLastInteraction();
	}
	
	public void incrementRequestAllowance() {
		if (requestAllowance < MAX_REQUEST_ALLOWANCE) requestAllowance++;
    	if (requestAllowance == MAX_REQUEST_ALLOWANCE) connectionBlocked = false;
	}
	
	public boolean expired() {
		return lastInteraction + EXPIRY_TIME_SECONDS < Instant.now().getEpochSecond();
	}
	
	public boolean connectionRateLimited() {
		updateLastInteraction();
		requestAllowance--;
		if (requestAllowance <= 0) {
			requestAllowance = 0;
			connectionBlocked = true;
		}
		return (requestAllowance == 0) | connectionBlocked;
	}
}