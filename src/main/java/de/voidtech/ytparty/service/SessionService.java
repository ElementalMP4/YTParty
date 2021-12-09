package main.java.de.voidtech.ytparty.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ephemeral.Session;

@Service
public class SessionService {

	private HashMap<String, Session> sessions = new HashMap<String, Session>();
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
		for (Session session : sessions.values()) {
			session.incrementRequestAllowance();
		}
	}
	
	public Session getSession(WebSocketSession session) {
		String ID = session.getId();
		return sessions.get(ID);
	}
	
	public void createSession(WebSocketSession session) {
		String ID = session.getId();
		Session newSession =  new Session();
		sessions.put(ID, newSession);
	}
	
	public void deleteSession(WebSocketSession session) {
		String ID = session.getId();
		sessions.remove(ID);
	}
}