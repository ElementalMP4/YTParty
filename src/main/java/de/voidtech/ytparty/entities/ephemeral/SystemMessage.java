package main.java.de.voidtech.ytparty.entities.ephemeral;

import org.json.JSONObject;

public class SystemMessage {
	
	private String type;
	
	private JSONObject data;
	
	public SystemMessage(String type, JSONObject data)
	{
	  this.type = type;
	  this.data = data;
	}
	
	public String convertToJSON() {
		JSONObject messageJsonObject = new JSONObject();
		messageJsonObject.put("type", "party-systemmessage");
		messageJsonObject.put("data", new JSONObject()
				.put("type", this.type)
				.put("data", this.data));
		return messageJsonObject.toString();
	}
}