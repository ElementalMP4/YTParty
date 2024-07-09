package main.java.de.voidtech.ytparty.entities;

import main.java.de.voidtech.ytparty.persistence.ChatMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GatewayConnection {

    private static final Logger LOGGER = Logger.getLogger(GatewayConnection.class.getName());

    private final WebSocketSession session;
    private String name;
    private int requestAllowance;
    private boolean connectionBlocked;
    private String roomID;

    private static final int MAX_REQUEST_ALLOWANCE = 20;

    public GatewayConnection(WebSocketSession session) {
        this.session = session;
        this.connectionBlocked = false;
        this.requestAllowance = MAX_REQUEST_ALLOWANCE;
    }

    public String getRoomID() {
        return this.roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WebSocketSession getSession() {
        return this.session;
    }

    public String getName() {
        return this.name;
    }

    public void incrementRequestAllowance() {
        if (requestAllowance < MAX_REQUEST_ALLOWANCE) requestAllowance++;
        if (requestAllowance == MAX_REQUEST_ALLOWANCE) connectionBlocked = false;
    }

    public boolean connectionRateLimited() {
        requestAllowance--;
        if (requestAllowance <= 0) {
            requestAllowance = 0;
            connectionBlocked = true;
        }
        return (requestAllowance == 0) | connectionBlocked;
    }

    public void sendError(String error, String origin) {
        try {
            getSession().sendMessage(new TextMessage(new JSONObject()
                    .put("success", false)
                    .put("response", error)
                    .put("type", origin)
                    .toString()));
        } catch (JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, "Could not respond to websocket: " + e.getMessage());
        }
    }

    public void sendSuccess(JSONObject message, String origin) {
        try {
            getSession().sendMessage(new TextMessage(new JSONObject()
                    .put("success", true)
                    .put("response", message)
                    .put("type", origin)
                    .toString()));
        } catch (JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, "Could not respond to websocket: " + e.getMessage());
        }
    }

    public void sendSuccess(String message, String origin) {
        try {
            getSession().sendMessage(new TextMessage(new JSONObject()
                    .put("success", true)
                    .put("response", message)
                    .put("type", origin)
                    .toString()));
        } catch (JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, "Could not respond to websocket: " + e.getMessage());
        }
    }

    public void sendChatHistory(List<ChatMessage> history) {
        try {
            for (ChatMessage message : history) {
                getSession().sendMessage(new TextMessage(message.convertToJson()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during Service Execution: " + e.getMessage());
        }
        sendSuccess(MessageBuilder.EMPTY_JSON, "party-partyready");
    }
}