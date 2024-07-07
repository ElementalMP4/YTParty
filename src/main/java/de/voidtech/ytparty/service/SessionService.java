package main.java.de.voidtech.ytparty.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.GatewayConnection;

@Service
public class SessionService {

	private HashMap<String, GatewayConnection> sessions = new HashMap<String, GatewayConnection>();
	private static final int REQUEST_INCREMENT_TIMER_DELAY = 5000;
	
	public SessionService() {		
		TimerTask requestAllowanceIncrement = new TimerTask() {
		    public void run() {
		    	Thread.currentThread().setName("Gateway Timer"); 
		    	incrementSessionRequestAllowance();
		    }
		 };
		
		Timer timer = new Timer();
		timer.schedule(requestAllowanceIncrement, REQUEST_INCREMENT_TIMER_DELAY, REQUEST_INCREMENT_TIMER_DELAY);
	}
	
	private void incrementSessionRequestAllowance() {
		for (GatewayConnection session : sessions.values()) {
			session.incrementRequestAllowance();
		}
	}
	
	public String getSessionRoomIDifExists(String username) {
		String roomID = sessions.values().stream()
				.filter(session -> username.equals(session.getName()))
				.map(GatewayConnection::getRoomID)
				.filter(id -> id != null)
				.findFirst()
				.orElse(null);
		return roomID;
	}
	
	public GatewayConnection getSession(WebSocketSession session) {
		return sessions.get(session.getId()); 
	}
	
	public void createSession(WebSocketSession session) {
		GatewayConnection newSession = new GatewayConnection(session);
		sessions.put(session.getId(), newSession);
	}
	
	public void deleteSession(WebSocketSession session) {
		sessions.remove(session.getId());
	}
}