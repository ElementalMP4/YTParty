package main.java.de.voidtech.ytparty.service;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ephemeral.Session;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;

@Service
public class MessageHandler {

	@Autowired
	private List<AbstractHandler> handlers;
	
	@Autowired
	private GatewayResponseService responder;
	
	private static final String RESPONSE_SOURCE = "Gateway";
	private static final int EXPIRED_SESSION_TIMER_DELAY = 30000;
	private static final int REQUEST_INCREMENT_TIMER_DELAY = 5000;
	private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
	
	private HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	public MessageHandler() {		
		TimerTask expiredSessionTask = new TimerTask() {
		    public void run() {
		    	removeExpiredSessions();
		    }
		 };
		
		TimerTask requestAllowanceIncrement = new TimerTask() {
		    public void run() {
		    	Thread.currentThread().setName("Gateway Timer");
		    	incrementSessionRequestAllowance();
		    }
		 };
		
		Timer timer = new Timer();
		timer.schedule(expiredSessionTask, EXPIRED_SESSION_TIMER_DELAY, EXPIRED_SESSION_TIMER_DELAY);
		timer.schedule(requestAllowanceIncrement, REQUEST_INCREMENT_TIMER_DELAY, REQUEST_INCREMENT_TIMER_DELAY);
	}
	
	private void removeExpiredSessions() {
		for (String address : sessions.keySet()) {
			if (sessions.get(address).expired()) {
				sessions.remove(address);
				LOGGER.log(Level.INFO, "Session at address " + address + " has expired");
			}
		}
		if (sessions.size() > 0) LOGGER.log(Level.INFO, "Session cleanup complete - Scanned " + sessions.size() + " sessions");
	}
	
	private void incrementSessionRequestAllowance() {
		for (Session session : sessions.values()) {
			session.incrementRequestAllowance();
		}
	}
	
	private Session getOrCreateSession(WebSocketSession session) {
		String address = session.getRemoteAddress().getAddress().getHostAddress();
		if (!sessions.containsKey(address)) {
			Session newSession =  new Session();
			sessions.put(address, newSession);
		}
		return sessions.get(address);
	}
	
	public void handleMessage(WebSocketSession session, String message) {
		try {
			Session rateSession = getOrCreateSession(session);
			JSONObject messageObject = new JSONObject(message);
			if (!messageObject.has("type") || !messageObject.has("data")) {
				responder.sendError(session, "Invalid message format", RESPONSE_SOURCE);
				return;
			}
				
			List<AbstractHandler> compatibleHandlers = handlers.stream()
					.filter(handler -> handler.getHandlerType().equals(messageObject.get("type")))
					.collect(Collectors.toList());
			
			if (!compatibleHandlers.isEmpty()) {
				AbstractHandler compatibleHandler = compatibleHandlers.get(0);
				LOGGER.log(Level.INFO, "Received Gateway Message: " + messageObject.getString("type"));
				if (compatibleHandler.requiresRateLimit()) {
					if (rateSession.connectionRateLimited()) {
						responder.sendError(session, "You are being rate limited!", RESPONSE_SOURCE);
						return;
					}
				}
				compatibleHandler.execute(session, messageObject.getJSONObject("data"));				
			} else responder.sendError(session, "Invalid message type", RESPONSE_SOURCE);
		} catch (JSONException e) {
			responder.sendError(session, "Invalid message - " + e.getMessage(), RESPONSE_SOURCE);
			LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
		}
	}	
}