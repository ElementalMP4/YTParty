package main.java.de.voidtech.ytparty.persistence;

import javax.persistence.*;


@Entity(name = "Tokens")
@Table(name = "Tokens")
public class TokenContainer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true)
	private String username; 
	
	@Column(unique = true)
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