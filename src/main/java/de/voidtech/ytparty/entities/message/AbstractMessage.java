package main.java.de.voidtech.ytparty.entities.message;

import org.json.JSONObject;

public abstract class AbstractMessage {
	
	public String convertToJson() {
		JSONObject messageJsonObject = new JSONObject();
		messageJsonObject.put("type", this.getMessageType());
		messageJsonObject.put("data", this.getMessageData());
		return messageJsonObject.toString();
	}
	
	public abstract String getMessageType();
	public abstract JSONObject getMessageData();
}
