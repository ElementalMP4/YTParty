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

import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.message.SystemMessage;
import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;

public class Party {
	
	private String partyID; 
	private String ownerName; 
	private String currentVideoID;
	private String roomColour;
	private List<GatewayConnection> sessions;
	private BlockingQueue<String> videoQueue;
	private boolean hasBeenVisited;
	private boolean ownerOnlyControlsEnabled;
	private int finishedCount;
	
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	
	public Party(String partyID, String ownerName, String roomColour, String videoID, boolean ownerOnlyControls) {
		this.partyID = partyID;
		this.roomColour = roomColour;
		this.ownerName = ownerName;
		this.currentVideoID = videoID;
		this.sessions = new ArrayList<GatewayConnection>();
		this.videoQueue = new LinkedBlockingQueue<String>();
		this.finishedCount = 0;
		this.ownerOnlyControlsEnabled = ownerOnlyControls;
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
		broadcastMessage(new MessageBuilder().type("party-changevideo").data(new JSONObject().put("video", nextVideo)).buildToSystemMessage());
	}
	
	public void enqueueVideo(String id) {
		this.videoQueue.offer(id);
	}
	
	public void skipVideo() {
		setNextVideo(this.videoQueue.poll());
	}
	
	public void clearQueue() {
		this.videoQueue.clear();
	}
	
	public List<String> getQueueAsList() {
		return new ArrayList<String>(this.videoQueue);
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
		return (ownerOnlyControlsEnabled ? this.ownerName.equals(username) : true);
	}
	
	public String getVideoID() {
		return this.currentVideoID;
	}
	
	public void setVideoID(String videoID) {
		this.currentVideoID = videoID;
	}
	
	public List<GatewayConnection> getAllSessions() {
		return sessions;
	}
	
	public void addToSessions(GatewayConnection session) {
		if (!hasBeenVisited) hasBeenVisited = true;
		this.sessions.add(session);
	}
	
	public void removeFromSessions(GatewayConnection session) {
		this.sessions.remove(session);
	}
	
	public void checkRemoveSession(GatewayConnection session) {
		if (sessions.contains(session)) { 
			sessions.remove(session);
			
			ChatMessage leftMessage = new MessageBuilder()
					.author(MessageBuilder.SYSTEM_AUTHOR)
					.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
					.colour(this.getRoomColour())
					.content(session.getName() + " has left the party!")
					.avatar(MessageBuilder.SYSTEM_AVATAR)
					.partyID(this.getPartyID())
					.buildToChatMessage();
			broadcastMessage(leftMessage);
		}
	}
	
	public boolean hasBeenVisited() {
		return this.hasBeenVisited;
	}
	
	public void broadcastMessage(ChatMessage message) {
		List<GatewayConnection> invalidSessions = new ArrayList<GatewayConnection>();
		for (GatewayConnection session : sessions) {
			if (session.getSession().isOpen()) sendMessage(message.convertToJson(), session.getSession());
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (GatewayConnection session : invalidSessions) sessions.remove(session);
	}
	
	public void broadcastMessage(SystemMessage message) {
		List<GatewayConnection> invalidSessions = new ArrayList<GatewayConnection>();
		for (GatewayConnection session : sessions) {
			if (session.getSession().isOpen()) sendMessage(message.convertToJson(), session.getSession());
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (GatewayConnection session : invalidSessions) sessions.remove(session);
	}
	
	public void sendMessage(String message, WebSocketSession session) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Gateway Execution: " + e.getMessage());
		}
	}
}
