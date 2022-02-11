package main.java.de.voidtech.ytparty.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;

@Service
public class MessageHandler {

	@Autowired
	private List<AbstractHandler> handlers; //Spring will gather all of our handlers and reference them here.
	
	@Autowired
	private GatewayResponseService responder; //We also need a standard interface for sending messages back to users.
	
	private static final String RESPONSE_SOURCE = "Gateway";
	//All messages from the server say where they are from. Errors from here will be from Gateway. Errors and messages from handlers
	//will use their respective handler name.
	private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName()); //We should log all gateway messages
	
	public void handleMessage(GatewayConnection session, String message) { //This is the method referenced earlier in the GatewayHandler
		try {
			JSONObject messageObject = new JSONObject(message); //Try and parse the message as a JSON object
			if (!messageObject.has("type") || !messageObject.has("data")) { //If the "type" and "data" fields are not found, reject.
				responder.sendError(session, "Invalid message format", RESPONSE_SOURCE); 
				return;
			}
				
			List<AbstractHandler> compatibleHandlers = handlers.stream()
					//Stream the handler list so we can find the necessary handler
					.filter(handler -> handler.getHandlerType().equals(messageObject.get("type")))
					//compare each handler type to the type specified in the message
					.collect(Collectors.toList());
					//collect a list of handlers
			
			if (!compatibleHandlers.isEmpty()) { //If we found a handler, we can run it!
				AbstractHandler compatibleHandler = compatibleHandlers.get(0); //Get the handler
				LOGGER.log(Level.INFO, "Received Gateway Message: " + messageObject.getString("type")); //Log the message type
				if (compatibleHandler.requiresRateLimit()) { //If this handler needs rate limiting, check now.
					if (session.connectionRateLimited()) { //If the connection is rate limited, stop the execution of the handler
						responder.sendError(session, "You are being rate limited!", RESPONSE_SOURCE);
						return;
					}
				} //If not, run the handler and pass in the custom session and the data object.
				compatibleHandler.execute(session, messageObject.getJSONObject("data"));
			} else responder.sendError(session, "Invalid message type", RESPONSE_SOURCE);
		} catch (JSONException e) { //If the message is not JSON, immediately reject it with an error.
			responder.sendError(session, "Invalid message - " + e.getMessage(), RESPONSE_SOURCE);
			LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
		}
	}	
}
