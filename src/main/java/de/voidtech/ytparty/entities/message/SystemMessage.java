package main.java.de.voidtech.ytparty.entities.message;

import org.json.JSONObject;

public class SystemMessage {
	
	private String type; //Store message type
	
	private JSONObject data; //Store message data
	
	public SystemMessage(MessageBuilder builder) //Automatically fill in fields from MessageBuilder
	{
	  this.type = builder.getSystemMessageType();
	  this.data = builder.getSystemMessageData();
	}

	public String convertToJson() { //Create a type-data representation of this message
		JSONObject data = new JSONObject()
				.put("type", this.type)
				.put("data", this.data);
		return data.toString();
	}
}