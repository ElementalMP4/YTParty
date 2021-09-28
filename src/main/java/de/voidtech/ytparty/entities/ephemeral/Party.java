package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
	
	private List<WebSocketSession> sessions;
	
	private BlockingQueue<String> videoQueue;
	
	private boolean hasBeenVisited;
	
	private String roomColour;
	
	private int finishedCount;
	
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	
	public Party(String partyID, String ownerName, String roomColour, String videoID)
	{
	  this.partyID = partyID;
	  this.roomColour = roomColour;
	  this.ownerName = ownerName;
	  this.currentVideoID = videoID;
	  this.sessions = new ArrayList<WebSocketSession>();
	  this.videoQueue = new LinkedBlockingQueue<String>();
	  this.finishedCount = 0;
	}
	
	public void incrementFinishedCount() {
		finishedCount = finishedCount + 1;
		if (finishedCount >= sessions.size()) {
			finishedCount = 0;
			String nextVideo = videoQueue.poll();
			if (nextVideo != null) setNextVideo(nextVideo);
		}
	}

	private void setNextVideo(String nextVideo) {
		this.currentVideoID = nextVideo;
		broadcastMessage(new SystemMessage("changevideo", new JSONObject().put("video", nextVideo)).convertToJSON());
	}
	
	public void enqueueVideo(String id) {
		this.videoQueue.offer(id);
	}
	
	public void skipVideo() {
		broadcastMessage(new SystemMessage("changevideo", new JSONObject().put("video", this.videoQueue.poll())).convertToJSON());
	}
	
	public BlockingQueue<String> getQueue() {
		return this.videoQueue;
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