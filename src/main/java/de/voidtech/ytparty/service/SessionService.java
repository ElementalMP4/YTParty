package main.java.de.voidtech.ytparty.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;

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
		List<GatewayConnection> connections = new ArrayList<GatewayConnection>();
		
		for (GatewayConnection connection : sessions.values()) {
			if (connection.getName().equals(username)) connections.add(connection);
		}
		
		for (GatewayConnection connection : connections) {
			if (connection.getRoomID() != null) return connection.getRoomID();
		}
		
		return null;
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