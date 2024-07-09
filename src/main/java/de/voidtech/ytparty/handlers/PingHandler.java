package main.java.de.voidtech.ytparty.handlers;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import org.json.JSONObject;

@Handler
public class PingHandler extends AbstractHandler {

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        long startTime = data.getLong("start");
        JSONObject pingData = new JSONObject()
                .put("start", startTime);
        session.sendSuccess(pingData, getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "system-ping";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }

}
