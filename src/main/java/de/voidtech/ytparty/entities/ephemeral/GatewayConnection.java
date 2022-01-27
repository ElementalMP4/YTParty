package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.time.Instant;

import org.springframework.web.socket.WebSocketSession;

public class GatewayConnection {
	
	private WebSocketSession session;
	private String name;
	private int requestAllowance;
	private long lastInteraction;
	private boolean connectionBlocked;
	private String sessionID;

	private static final int EXPIRY_TIME_SECONDS = 900; 
	private static final int MAX_REQUEST_ALLOWANCE = 20;

	
	public GatewayConnection(WebSocketSession session) {
		this.session = session;
		this.connectionBlocked = false;
		this.requestAllowance = MAX_REQUEST_ALLOWANCE;
		this.sessionID = session.getId();
		updateLastInteraction();
	}
	
	public String getID() {
		return this.sessionID;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public WebSocketSession getSession() {
		return this.session;
	}
	
	public String getName() {
		return this.name;
	}

	private void updateLastInteraction() {
		lastInteraction = Instant.now().getEpochSecond();
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