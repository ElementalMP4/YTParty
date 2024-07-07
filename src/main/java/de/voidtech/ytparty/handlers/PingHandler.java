package main.java.de.voidtech.ytparty.handlers;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class PingHandler extends AbstractHandler {

	@Autowired
	private GatewayResponseService responder;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		long startTime = data.getLong("start");
		
		JSONObject pingData = new JSONObject()
				.put("start", startTime);
		
		responder.sendSuccess(session, pingData, getHandlerType());
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
