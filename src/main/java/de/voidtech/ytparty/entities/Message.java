package main.java.de.voidtech.ytparty.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity(name = "Messages")
@Table(name = "Messages", indexes = @Index(columnList = "partyID", name = "index_message"))
public class Message {
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
	private String content;
	
	@Column
	private String messageModifiers;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	Message() {
	}
	
	public Message(String partyID, String author, String colour, String content, String messageModifiers)
	{
	  this.partyID = partyID;
	  this.author = author;
	  this.colour = colour;
	  this.content = content;
	  this.messageModifiers = messageModifiers;
	}

	public String getPartyID() {
		return this.partyID;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public String getMessageModifiers() {
		return this.messageModifiers;
	}
	
	public void setPartyID(String newPartyID) {
		this.partyID = newPartyID;
	}
	
	public void setAuthor(String newAuthor) {
		this.author = newAuthor;
	}
	
	public void setColour(String newColour) {
		this.colour = newColour;
	}
	
	public void setContent(String newContent) {
		this.content = newContent;
	}
	
	public void setMessageModifiers(String newModifiers) {
		this.messageModifiers = newModifiers;
	}
}