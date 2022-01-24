package main.java.de.voidtech.ytparty.handlers;

import org.json.JSONObject;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;

public abstract class AbstractHandler {
	public abstract void execute(GatewayConnection session, JSONObject data);
	public abstract String getHandlerType();
	public abstract boolean requiresRateLimit();
}
