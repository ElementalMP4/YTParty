package main.java.de.voidtech.ytparty.communication.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import main.java.de.voidtech.ytparty.service.MessageHandler;

@Component
public class GatewayHandler extends AbstractWebSocketHandler {
	
	@Autowired
	private MessageHandler messageHandler;
	
	List<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
			messageHandler.handleMessage(session, message.getPayload());			
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
	    sessions.remove(session);
	}

}
