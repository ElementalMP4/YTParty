package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class VideoEndHandler extends AbstractHandler {

    @Autowired
    private PartyService partyService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String roomID = data.getString("roomID");

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
        party.incrementFinishedCount();

    }

    @Override
    public String getHandlerType() {
        return "party-videoend";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }

}
