package main.java.de.voidtech.ytparty.persistence;

import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import org.hibernate.annotations.Type;
import org.json.JSONObject;

import javax.persistence.*;

@Entity(name = "Messages")
@Table(name = "Messages", indexes = @Index(columnList = "partyID", name = "index_message"))
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String partyID; 
	
	@Column
	private String author;
	
	@Column
	private String colour;
	
	@Column
	@Type(type = "org.hibernate.type.TextType")

	private String content;
	
	@Column
	private String avatar;
	
	@Deprecated
	ChatMessage() {
	}

	public String getPartyID() {
		return partyID;
	}

	public void setPartyID(String partyID) {
		this.partyID = partyID;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	public ChatMessage(MessageBuilder builder)
	{
	  this.partyID = builder.getChatMessagePartyID();
	  this.author = builder.getChatMessageAuthor();
	  this.colour = builder.getChatMessageColour();
	  this.content = builder.getChatMessageContent();
	  this.avatar = builder.getChatMessageAvatar();
	}

	public String convertToJson() {
		JSONObject data = new JSONObject().put("type", "party-chatmessage")
				.put("data", new JSONObject()
						.put("author", this.author)
						.put("colour", this.colour)
						.put("content", this.content)
						.put("avatar", this.avatar));
		return data.toString();
	}
}