package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PasswordResetCase {
	
	private String token;
	private String user;
	private long creationTime;
	
	private static final String LEXICON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890"; //Case ID lexicon
	private static final List<String> LEXICON = Arrays.asList(LEXICON_CHARS.split(""));
	private static final long EXPIRY_DURATION = 900; //15 Minutes 
	
	public PasswordResetCase(String user) {
		this.creationTime = Instant.now().getEpochSecond(); //Set the creation time
		this.token = generatePasswordResetToken(); //generate a reset token
		this.user = user; //Set the user 
	}
	
	private String generatePasswordResetToken() {
    	String token = "";
		Random random = new SecureRandom();
		for (int i = 0; i < 40; i++) token += LEXICON.get(random.nextInt(LEXICON.size() - 1));
		return token;
	}
	
	public String getUser() {
		return this.user; //Get the user
	}
	
	public String getToken() {
		return this.token; //Get the token
	}
	
	public boolean expired() { //Check the expriy time, if this case is 15 minutes old then it will be erased.
		return this.creationTime + EXPIRY_DURATION < Instant.now().getEpochSecond();
	}
	
}
