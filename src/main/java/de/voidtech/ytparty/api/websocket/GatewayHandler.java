package main.java.de.voidtech.ytparty.api.websocket;

import main.java.de.voidtech.ytparty.service.MessageHandler;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class GatewayHandler extends AbstractWebSocketHandler {
	
	@Autowired
	private MessageHandler messageHandler;
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private SessionService sessionService;
	
	@Override
	public void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
		messageHandler.handleMessage(sessionService.getSession(socketSession), message.getPayload());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionService.createSession(session);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		partyService.removeSessionFromParty(sessionService.getSession(session));
		sessionService.deleteSession(session);
	}
}