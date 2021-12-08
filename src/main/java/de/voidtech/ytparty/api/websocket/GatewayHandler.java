package main.java.de.voidtech.ytparty.api.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import main.java.de.voidtech.ytparty.service.MessageHandler;
import main.java.de.voidtech.ytparty.service.PartyService;

@Component
public class GatewayHandler extends AbstractWebSocketHandler {
	
	@Autowired
	private MessageHandler messageHandler;
	
	@Autowired
	private PartyService partyService;
	
	private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

	@Override
	public void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
		messageHandler.handleMessage(socketSession, message.getPayload());			
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession socketSession) throws Exception {
		String address = socketSession.getRemoteAddress().getAddress().getHostAddress();
		LOGGER.log(Level.INFO, "New session from " + address);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		partyService.removeSessionFromParty(session);
		String address = session.getRemoteAddress().getAddress().getHostAddress();
		LOGGER.log(Level.INFO, "Session at " + address + " has terminated");
	}
}