package main.java.de.voidtech.ytparty.entities.ephemeral;

public class AuthResponse {

	private boolean success;
	private String message;
	private String actingString;
	
	public String getMessage() {
		return this.success ? "Success" : this.message;
	}
	
	public boolean isSuccessful() {
		return this.success;
	}
	
	public void setActingString(String actingString) {
		this.actingString = actingString;
	}
	
	public String getActingString() {
		return this.actingString;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
