package main.java.de.voidtech.ytparty.entities.message;

import org.json.JSONObject;

import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;

public class MessageBuilder {
	

	private String partyID; 
	private String author;
	private String colour;
	private String content;
	private String avatar;
	

	private String type;
	private JSONObject data;
	

	public static final String SYSTEM_AUTHOR = "System";
	public static final String SYSTEM_AVATAR = "system";
	

	public static final JSONObject EMPTY_JSON = new JSONObject();
	
	public String getChatMessagePartyID() {
		return this.partyID;
	}
	
	public String getChatMessageAuthor() {
		return this.author;
	}
	
	public String getChatMessageColour() {
		return this.colour;
	}
	
	public String getChatMessageContent() {
		return this.content;
	}
	
	public String getChatMessageAvatar() {
		return this.avatar;
	}
	
	public MessageBuilder partyID(String partyID) {
		this.partyID = partyID;
		return this;
	}
	
	public MessageBuilder author(String author) {
		this.author = author;
		return this;
	}
	
	public MessageBuilder colour(String colour) {
		this.colour = colour;
		return this;
	}
	
	public MessageBuilder content(String content) {
		this.content = content;
		return this;
	}
	
	public MessageBuilder type(String type) {
		this.type = type;
		return this;
	}
	
	public MessageBuilder data(JSONObject data) {
		this.data = data;
		return this;
	}
	
	public MessageBuilder avatar(String avatar) {
		this.avatar = avatar;
		return this;
	}
	
	public ChatMessage buildToChatMessage() {
		return new ChatMessage(this);
	}
	
	public SystemMessage buildToSystemMessage() {
		return new SystemMessage(this);
	}

	public String getSystemMessageType() {
		return this.type;
	}

	public JSONObject getSystemMessageData() {
		return this.data;
	}
}
