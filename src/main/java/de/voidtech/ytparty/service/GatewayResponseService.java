package main.java.de.voidtech.ytparty.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ChatMessage;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.entities.SystemMessage;

@Service
public class GatewayResponseService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final Logger LOGGER = Logger.getLogger(GatewayResponseService.class.getName());

	public void sendError(WebSocketSession session, String error, String origin) {
		try {
			session.sendMessage(new TextMessage(new JSONObject().put("success", false).put("response", error).put("origin", origin).toString()));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
		}
	}
	
	public void sendSuccess(WebSocketSession session, String message, String origin) {
		try {
			session.sendMessage(new TextMessage(new JSONObject().put("success", true).put("response", message).put("origin", origin).toString()));
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
		}
	}
	
	public void sendChatMessage(Party party, ChatMessage message) {
		party.broadcastMessage(message.convertToJSON());
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(message);
			session.getTransaction().commit();
		}
	}
	
	public void sendSystemMessage(Party party, SystemMessage systemMessage) {
		party.broadcastMessage(systemMessage.convertToJSON());
	}
}