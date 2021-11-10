package main.java.de.voidtech.ytparty.entities.message;

public enum MessageType {
	SYSTEM("party-systemmessage"),
	CHAT("party-chatmessage");
	
	private final String type;
	
	MessageType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}