package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.time.Instant;

import org.springframework.web.socket.WebSocketSession;

public class GatewayConnection {
	
	private WebSocketSession session; //Store the WebSocket session 
	private String name; //Store the username of the session (for use in parties)
	private int requestAllowance; //Store how many requests this session has available
	private long lastInteraction; //Store how long ago the last interaction was
	private boolean connectionBlocked; //Store whether to block messages from this session
	private String sessionID; //Store the unique ID of this session

	private static final int EXPIRY_TIME_SECONDS = 900; //Keep the expiry time constant
	private static final int MAX_REQUEST_ALLOWANCE = 20; //As well as the number of requests that can be made in a certain time frame.

	
	public GatewayConnection(WebSocketSession session) {
		this.session = session;
		this.connectionBlocked = false;
		this.requestAllowance = MAX_REQUEST_ALLOWANCE;
		this.sessionID = session.getId();
		updateLastInteraction(); //Always update the last interaction so we can accurately tell when this session has become inactive.
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
		//Set the last interaction to the current time in seconds
	}

	public void incrementRequestAllowance() {
		if (requestAllowance < MAX_REQUEST_ALLOWANCE) requestAllowance++;
		if (requestAllowance == MAX_REQUEST_ALLOWANCE) connectionBlocked = false;
		//If the connection has been blocked, we will only unblock it once the request allowance is full again. 
	}

	public boolean expired() {
		return lastInteraction + EXPIRY_TIME_SECONDS < Instant.now().getEpochSecond();
		//We can tell if this session is expired by adding the expiry duration to the last interaction
	}

	public boolean connectionRateLimited() {
		updateLastInteraction();
		requestAllowance--;
		if (requestAllowance <= 0) {
			requestAllowance = 0;
			connectionBlocked = true;
		}
		return (requestAllowance == 0) | connectionBlocked;
		//Even if we have an allowance, it will not be accessible if the connection is blocked.
	}
}