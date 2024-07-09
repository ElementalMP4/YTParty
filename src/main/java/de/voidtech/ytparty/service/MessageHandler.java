package main.java.de.voidtech.ytparty.service;

import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageHandler {

    @Autowired
    private List<AbstractHandler> handlers;

    private static final String RESPONSE_SOURCE = "Gateway";
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    public void handleMessage(GatewayConnection session, String message) {
        try {
            JSONObject messageObject = new JSONObject(message);
            if (!messageObject.has("type") || !messageObject.has("data")) {
                session.sendError("Invalid message format", RESPONSE_SOURCE);
                return;
            }

            Optional<AbstractHandler> potentialHandler = handlers.stream()
                    .filter(handler -> handler.getHandlerType().equals(messageObject.get("type")))
                    .findFirst();

            if (potentialHandler.isEmpty()) {
                session.sendError("Invalid message type", RESPONSE_SOURCE);
                return;
            }

            AbstractHandler compatibleHandler = potentialHandler.get();
            if (compatibleHandler.requiresRateLimit()) {
                if (session.connectionRateLimited()) {
                    session.sendError("You are being rate limited!", RESPONSE_SOURCE);
                    return;
                }
            }
            compatibleHandler.execute(session, messageObject.getJSONObject("data"));

        } catch (JSONException e) {
            session.sendError("Invalid message - " + e.getMessage(), RESPONSE_SOURCE);
            LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
        }
    }
}
