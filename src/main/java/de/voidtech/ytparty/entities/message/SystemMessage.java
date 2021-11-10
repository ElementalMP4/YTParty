package main.java.de.voidtech.ytparty.entities.message;

import org.json.JSONObject;

public class SystemMessage extends AbstractMessage {
	
	private String type;
	
	private JSONObject data;
	
	public SystemMessage(MessageBuilder builder)
	{
	  this.type = builder.getSystemMessageType();
	  this.data = builder.getSystemMessageData();
	}
	
	@Override
	public JSONObject getMessageData() {
		JSONObject data = new JSONObject()
				.put("type", this.type)
				.put("data", this.data);
		return data;
	}

	@Override
	public String getMessageType() {
		return "party-systemmessage";
	}
}