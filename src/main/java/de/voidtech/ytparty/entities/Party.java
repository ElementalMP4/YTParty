package main.java.de.voidtech.ytparty.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Party {
	
	private String partyID; 
	
	private String ownerName; 
	
	private String currentVideoID;
	
	private List<WebSocketSession> sessions;
	
	private boolean hasBeenVisited;
	
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	
	public Party(String partyID, String ownerName, String videoID)
	{
	  this.partyID = partyID;
	  this.ownerName = ownerName;
	  this.currentVideoID = videoID;
	  this.sessions = new ArrayList<WebSocketSession>();
	}

	public String getPartyID() {
		return this.partyID;
	}
	
	public String getOwnerName() {
		return this.ownerName;
	}
	
	public String getVideoID() {
		return this.currentVideoID;
	}
	
	public List<WebSocketSession> getAllSessions() {
		return sessions;
	}
	
	public void addToSessions(WebSocketSession session) {
		if (!hasBeenVisited) hasBeenVisited = true;
		this.sessions.add(session);
	}
	
	public void removeFromSessions(WebSocketSession session) {
		this.sessions.remove(session);
	}
	
	public void broadcastMessage(String message) {
		List<WebSocketSession> invalidSessions = new ArrayList<WebSocketSession>();
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) sendChatMessage(message, session);
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (WebSocketSession session : invalidSessions) sessions.remove(session);
	}

	public void checkRemoveSession(WebSocketSession session) {
		if (sessions.contains(session)) { 
			sessions.remove(session);
			broadcastMessage(new ChatMessage(this.partyID, "System", "#ff0000", "Someone has left the party!", "system").convertToJSON());
		}
	}
	
	public boolean hasBeenVisited() {
		return this.hasBeenVisited;
	}
	
	public void sendChatMessage(String message, WebSocketSession session) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Gateway Execution: " + e.getMessage());
		}
	}
}