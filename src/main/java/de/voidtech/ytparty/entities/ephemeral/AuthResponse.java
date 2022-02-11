package main.java.de.voidtech.ytparty.entities.ephemeral;

public class AuthResponse {

	private boolean success;
	private String message;
	private String actingString;
	
	public String getMessage() {
		return this.success ? "Success" : this.message; //If the response is successful, we don't need to explain it.
	}
	
	public boolean isSuccessful() {
		return this.success;
	}
	
	public void setActingString(String actingString) {
		this.actingString = actingString;
	}
	
	public String getActingString() {
		return this.actingString; //The acting string is either a username returned from validating a token, or a room ID.
		//Storing the acting string like this allows us to minimise database traffic. 
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
