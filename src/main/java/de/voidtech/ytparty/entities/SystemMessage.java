package main.java.de.voidtech.ytparty.entities;

import org.json.JSONObject;

public class SystemMessage {
	
	private final String type;
	
	private final JSONObject data;
	
	public SystemMessage(MessageBuilder builder)
	{
	  this.type = builder.getSystemMessageType();
	  this.data = builder.getSystemMessageData();
	}

	public String convertToJson() {
		JSONObject data = new JSONObject()
				.put("type", this.type)
				.put("data", this.data);
		return data.toString();
	}
}