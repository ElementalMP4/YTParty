package main.java.de.voidtech.ytparty.entities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.service.GatewayResponseService;

public class Party {
	
	private String partyID; 
	
	private String ownerName; 
	
	private String currentVideoID;
	
	private List<WebSocketSession> sessions;
	
	@Autowired
	private GatewayResponseService responder;
	
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
		this.sessions.add(session);
	}
	
	public void removeFromSessions(WebSocketSession session) {
		this.sessions.remove(session);
	}
	
	public void setPartyID(String partyID) {
		this.partyID = partyID;
	}
	
	public void setPartyOwner(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public void setVideoID(String newVideoID) {
		this.currentVideoID = newVideoID;
	}
	
	public void broadcastMessage(JSONObject message) {
		List<WebSocketSession> invalidSessions = new ArrayList<WebSocketSession>();
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) responder.sendSuccess(session, message.toString(), "party");
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (WebSocketSession session : invalidSessions) sessions.remove(session);
	}
}