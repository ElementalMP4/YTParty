package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;

public class Party {
	
	private String partyID; 
	private String ownerName; 
	private String currentVideoID;
	private String roomColour;
	private List<WebSocketSession> sessions;
	private Queue videoQueue;
	private boolean hasBeenVisited;
	private int finishedCount;
	
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	
	public Party(String partyID, String ownerName, String roomColour, String videoID)
	{
	  this.partyID = partyID;
	  this.roomColour = roomColour;
	  this.ownerName = ownerName;
	  this.currentVideoID = videoID;
	  this.sessions = new ArrayList<WebSocketSession>();
	  this.videoQueue = new Queue();
	  this.finishedCount = 0;
	}
	
	public void incrementFinishedCount() {
		finishedCount = finishedCount + 1;
		if (finishedCount >= sessions.size()) {
			finishedCount = 0;
			String nextVideo = videoQueue.pop();
			if (nextVideo != null) setNextVideo(nextVideo);
		}
	}

	private void setNextVideo(String nextVideo) {
		this.currentVideoID = nextVideo;
		broadcastMessage(new SystemMessage("changevideo", new JSONObject().put("video", nextVideo)).convertToJSON());
	}
	
	public void enqueueVideo(String id) {
		this.videoQueue.appendItem(id);
	}
	
	public void skipVideo() {
		broadcastMessage(new SystemMessage("changevideo", new JSONObject().put("video", this.videoQueue.pop())).convertToJSON());
	}
	
	public void clearQueue() {
		this.videoQueue.clear();
	}
	
	public List<String> getQueueAsList() {
		return this.videoQueue.getAsList();
	}
	
	public boolean queueIsEmpty() {
		return this.videoQueue.isEmpty();
	}

	public String getPartyID() {
		return this.partyID;
	}
	
	public String getRoomColour() {
		return this.roomColour;
	}

	public String getOwnerName() {
		return this.ownerName;
	}
	
	public boolean canControlRoom(String username) {
		return this.ownerName == null ? true : this.ownerName.equals(username);
	}
	
	public String getVideoID() {
		return this.currentVideoID;
	}
	
	public void setVideoID(String videoID) {
		this.currentVideoID = videoID;
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
	
	public void checkRemoveSession(WebSocketSession session) {
		if (sessions.contains(session)) { 
			sessions.remove(session);
			broadcastMessage(new ChatMessage(this.partyID, "System", "#ff0000", "Someone has left the party!", "system").convertToJSON());
		}
	}
	
	public boolean hasBeenVisited() {
		return this.hasBeenVisited;
	}
	
	public void broadcastMessage(String message) {
		List<WebSocketSession> invalidSessions = new ArrayList<WebSocketSession>();
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				sendMessage(message, session);
			}
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (WebSocketSession session : invalidSessions) sessions.remove(session);
	}
	
	public void sendMessage(String message, WebSocketSession session) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Gateway Execution: " + e.getMessage());
		}
	}
}