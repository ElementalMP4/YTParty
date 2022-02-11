package main.java.de.voidtech.ytparty.api.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import main.java.de.voidtech.ytparty.service.MessageHandler;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.SessionService;

@Component
public class GatewayHandler extends AbstractWebSocketHandler { //This class extends an abstract class. It will need overriding methods.
	
	@Autowired
	private MessageHandler messageHandler; //Inject the message handler to decide what to do with a message.
	
	@Autowired
	private PartyService partyService; //Inject the party service for removing dead sessions from the party, AKA a user disconnecting.
	
	@Autowired
	private SessionService sessionService; //Inject the SessionService. This keeps track of which user is on which session.
	
	@Override
	public void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
		messageHandler.handleMessage(sessionService.getSession(socketSession), message.getPayload());
		//The session service will return the custom session wrapper for this connection.
		//We pass the custom session into the message handler along with the text message we just received. 
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionService.createSession(session);
		//When a user connects, we need to keep track of their session. We can store their username in a custom class with this 
		//WebSocketSession so we know who leaves a party.
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		partyService.removeSessionFromParty(sessionService.getSession(session));
		sessionService.deleteSession(session);
		//When the session is closed, we need to check each party to see if the session was inside one of them. If it was, we will send
		//a user leaving message. Then, we remove this session from our session registry.
	}
}