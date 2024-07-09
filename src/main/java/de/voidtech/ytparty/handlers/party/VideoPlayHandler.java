package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class VideoPlayHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private PartyService partyService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String roomID = data.getString("roomID");
        int timestamp = data.getInt("timestamp");

        AuthResponse tokenResponse = authService.validateToken(token);
        AuthResponse partyIDResponse = authService.validatePartyID(roomID);

        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }
        if (!partyIDResponse.isSuccessful()) {
            session.sendError(partyIDResponse.getMessage(), this.getHandlerType());
            return;
        }

        Party party = partyService.getParty(roomID);
        if (!party.canControlRoom(tokenResponse.getActingString())) {
            session.sendError("You're not allowed to start the video!", this.getHandlerType());
            return;
        }

        partyService.sendSystemMessage(party, new MessageBuilder()
                .type("party-playvideo")
                .data(new JSONObject().put("time", timestamp))
                .buildToSystemMessage());
    }

    @Override
    public String getHandlerType() {
        return "party-playvideo";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }
}