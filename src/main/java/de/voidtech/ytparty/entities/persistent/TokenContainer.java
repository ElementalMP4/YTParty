package main.java.de.voidtech.ytparty.entities.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//Set the table name
@Entity(name = "Tokens")
@Table(name = "Tokens")
public class TokenContainer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true) //Usernames MUST be unique.
	private String username; 
	
	@Column(unique = true) //Tokens MUST also be unique.
	private String token; 
	
	@Deprecated
	TokenContainer() {
	}
	
	public TokenContainer(String username, String generatedToken)
	{
	  this.username = username;
	  this.token = generatedToken;
	}
	
	public String getUsername() {
		return this.username; 
	}
	
	public String getToken() {
		return this.token;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
}