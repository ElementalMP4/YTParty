package main.java.de.voidtech.ytparty.handlers;

import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import org.json.JSONObject;

public abstract class AbstractHandler { //Label the class as abstract
	public abstract void execute(GatewayConnection session, JSONObject data); //This method is the entry point where we can run the handler.
	public abstract String getHandlerType(); //This method returns the name of the handler.
	public abstract boolean requiresRateLimit(); //This method notifies the MessageHandler about whether this needs rate limiting.
}

