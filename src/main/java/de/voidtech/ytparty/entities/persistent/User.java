package main.java.de.voidtech.ytparty.entities.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.crypto.bcrypt.BCrypt;

@Entity(name = "Users")
@Table(name = "Users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true)
	private String username; 
	
	@Column
	private String nickname; 
	
	@Column
	private String passwordHash;
	
	@Column
	private String hexColour;
	
	@Column
	private String email;
	
	@Column
	private String profilePicture;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	User() {
	}
	
	public User(String username, String nickname, String password, String hexColour, String email, String avatar)
	{
	  this.username = username;
	  this.nickname = nickname;
	  this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
	  this.hexColour = hexColour;
	  this.email = email;
	  this.profilePicture = avatar;
	}

	public boolean checkPassword(String enteredPassword) {
		return BCrypt.checkpw(enteredPassword, this.passwordHash);
	}
	
	public String getProfilePicture() {
		return this.profilePicture;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getNickname() {
		return this.nickname;
	}
	
	public String getHexColour() {
		return this.hexColour;
	}
	
	public void setPassword(String password) {
		this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public void setHexColour(String newHexColour) {
		this.hexColour = newHexColour;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}
	
	public String getEffectiveName() {
		return this.nickname == null ? this.username : this.nickname;
	}
}