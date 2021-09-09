package main.java.de.voidtech.ytparty.entities;

public class AuthResponse {

	private boolean success;
	private String message;
	
	public String getMessage() {
		return !this.success ? this.message : "Success";
	}
	
	public boolean isSuccessful() {
		return this.success;
	}	
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}