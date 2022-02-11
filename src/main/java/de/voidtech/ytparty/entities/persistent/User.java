package main.java.de.voidtech.ytparty.entities.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.crypto.bcrypt.BCrypt;

//Set the table and entity name to "Users" for hibernate.
@Entity(name = "Users")
@Table(name = "Users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true)
	private String username;
	//Usernames are unique, they are used to identify a user account
	
	@Column
	private String nickname;
	//Nicknames are not unique, many users can share a nickname
	
	@Column
	private String passwordHash;
	//To protect users, their passwords are irreversibly hashed.
	
	@Column
	private String hexColour;
	//The user may choose a colour that their name will appear as in chat messages
	
	@Column
	private String email;
	//If a user signed up with their email, it will be stored here.
	
	@Column
	private String profilePicture;
	//The avatar choice of a user is stored here
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	User() {
	}
	
	public User(String username, String nickname, String password, String hexColour, String email, String avatar)
	{
	  this.username = username;
	  this.nickname = nickname;
	  this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt()); //Create password hash
	  this.hexColour = hexColour;
	  this.email = email;
	  this.profilePicture = avatar;
	}

	public boolean checkPassword(String enteredPassword) {
		return BCrypt.checkpw(enteredPassword, this.passwordHash);
		//The password is not readable anymore, so we need to compare an inputted password to the stored hash. 
	}
	
	public void setPassword(String password) {
		this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
		//If the password is changed, we need to re-hash it
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
		//We use this to get the name to show in chat messages.
		//It saves some processing on the client side, making the client simpler. 
	}
}