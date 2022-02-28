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

import main.java.de.voidtech.ytparty.entities.message.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.message.SystemMessage;
import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;

public class Party {
	private String partyID; //Contains the ID for this room
	private String ownerName; //Contains the name of the room owner
	private String currentVideoID; //Contains the ID of the current video
	private String roomColour; //Contains the HEX code for the room theme
	private List<GatewayConnection> sessions; //Contains all the people who have joined this room
	private Queue<String> videoQueue; //Contains all the queued video IDs
	//If this room has not yet been visited, this boolean will protect the room from being automatically deleted
	private boolean hasBeenVisited; 
	private boolean ownerOnlyControlsEnabled; //This boolean controls whether other users can play/pause the video etc.
	//We increment this value as people's players finish. When this value is equal to the number of members in the room, we can load the next video
	private int finishedCount;
	
	//We use a logger for the gateway methods as we cannot autowire services into dynamic classes
	private static final Logger LOGGER = Logger.getLogger(Party.class.getName());
	
	//The constructor takes all the values from the CreateRoom menu and makes a new room
	public Party(String partyID, String ownerName, String roomColour, String videoID, boolean ownerOnlyControls) {
		this.partyID = partyID;
		this.roomColour = roomColour;
		this.ownerName = ownerName;
		this.currentVideoID = videoID;
		this.sessions = new ArrayList<GatewayConnection>(); //Create a new sessions list
		this.videoQueue = new Queue<String>(); //Create an empty queue
		this.finishedCount = 0;
		this.ownerOnlyControlsEnabled = ownerOnlyControls;
	}
	
	//This method increments the finished count when someone finishes a video.
	public void incrementFinishedCount() {
		finishedCount = finishedCount + 1; //Increment the count
		if (finishedCount >= sessions.size()) { //Check if the count is at the target value
			finishedCount = 0; //Reset the count
			String nextVideo = videoQueue.pop(); //Get the next video
			if (nextVideo != null) setNextVideo(nextVideo); //If there is a next video, load it 
		}
	}

	private void setNextVideo(String nextVideo) { //This method loads videos on the client
		this.currentVideoID = nextVideo; //Set the current video ID
		broadcastMessage(new MessageBuilder() //Create a new system message which contains the video we want to play, and broadcast it
				.type("party-changevideo")
				.data(new JSONObject()
						.put("video", nextVideo))
				.buildToSystemMessage());
	}
	
	public void enqueueVideo(String id) {
		this.videoQueue.push(id);
	}
	
	public void skipVideo() {
		setNextVideo(this.videoQueue.pop());
	}
	
	public void clearQueue() {
		this.videoQueue.clear();
	}
	
	//This method converts the queue to a List so it can be displayed to the user
	public List<String> getQueueAsList() {
		return this.videoQueue.toList();
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
	
	//This method checks if the provided user is allowed to control the room
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
		if (!hasBeenVisited) hasBeenVisited = true; //We need to mark this room as visited as soon as someone joins
		this.sessions.add(session);
	}
	
	public void removeFromSessions(GatewayConnection session) {
		this.sessions.remove(session);
	}
	
	//This method checks to see if a disconnected session is in the party
	public void checkRemoveSession(GatewayConnection session) {
		if (sessions.contains(session)) { //If the session is in this party, remove it from the session list
			sessions.remove(session);
			
			ChatMessage leftMessage = new MessageBuilder() //Create a new message to say that this user has left
					.author(MessageBuilder.SYSTEM_AUTHOR)
					.modifiers(MessageBuilder.SYSTEM_MODIFIERS)
					.colour(this.getRoomColour())
					.content(session.getName() + " has left the party!")
					.avatar(MessageBuilder.SYSTEM_AVATAR)
					.partyID(this.getPartyID())
					.buildToChatMessage();
			broadcastMessage(leftMessage); //Send the message
		}
	}
	
	public boolean hasBeenVisited() {
		return this.hasBeenVisited;
	}
	
	public void broadcastMessage(ChatMessage message) {
		//In case any sessions have closed and we did not detect it, we should check them and then remove them if necessary
		List<GatewayConnection> invalidSessions = new ArrayList<GatewayConnection>();
		for (GatewayConnection session : sessions) { //Iterate every session
			if (session.getSession().isOpen()) sendMessage(message.convertToJson(), session.getSession()); //Send the message
			else invalidSessions.add(session); //Filter invalid sessions
		}
		//Remove invalid sessions
		if (!invalidSessions.isEmpty()) for (GatewayConnection session : invalidSessions) sessions.remove(session);
	}
	
	//We use the same method as before, but this method takes a SystemMessage instead
	public void broadcastMessage(SystemMessage message) {
		List<GatewayConnection> invalidSessions = new ArrayList<GatewayConnection>();
		for (GatewayConnection session : sessions) {
			if (session.getSession().isOpen()) sendMessage(message.convertToJson(), session.getSession());
			else invalidSessions.add(session);
		}
		if (!invalidSessions.isEmpty()) for (GatewayConnection session : invalidSessions) sessions.remove(session);
	}
	
	//Send a string message to a websocket session
	public void sendMessage(String message, WebSocketSession session) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Gateway Execution: " + e.getMessage()); //Log any errors
		}
	}
}
