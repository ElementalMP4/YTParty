package main.java.de.voidtech.ytparty.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;

@Service
public class SessionService {

	private HashMap<String, GatewayConnection> sessions = new HashMap<String, GatewayConnection>();
	//Store sessions with their ID for speed. It is much faster to access a HashMap than to stream a List.
	private static final int REQUEST_INCREMENT_TIMER_DELAY = 5000; //How long it takes for the request allowance to gain one extra request
	
	public SessionService() {		
		TimerTask requestAllowanceIncrement = new TimerTask() {
		    public void run() {
		    	Thread.currentThread().setName("Gateway Timer"); //Set the name of this thread so we can identify it if necessary
		    	incrementSessionRequestAllowance(); //Increment every session's allowance
		    }
		 };
		
		Timer timer = new Timer(); //We create a new timer to fill up the request allowances
		//This timer starts in a new thread, separate from main.
		timer.schedule(requestAllowanceIncrement, REQUEST_INCREMENT_TIMER_DELAY, REQUEST_INCREMENT_TIMER_DELAY);
	}
	
	private void incrementSessionRequestAllowance() {
		for (GatewayConnection session : sessions.values()) { //Iterate over every session
			session.incrementRequestAllowance(); //Increment the allowance of this session
		}
	}
	
	public GatewayConnection getSession(WebSocketSession session) {
		return sessions.get(session.getId()); //Get a session by its unique ID
	}
	
	public void createSession(WebSocketSession session) {
		GatewayConnection newSession = new GatewayConnection(session); //Create a new GatewayConnection
		sessions.put(session.getId(), newSession); //Store it in the hashMap
	}
	
	public void deleteSession(WebSocketSession session) {
		sessions.remove(session.getId()); //Delete the session once it is closed
	}
}