package main.java.de.voidtech.ytparty.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class GatewayResponseService {
	
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
}