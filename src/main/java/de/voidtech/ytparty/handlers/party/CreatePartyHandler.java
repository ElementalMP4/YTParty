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

import java.util.logging.Level;
import java.util.logging.Logger;

@Handler
public class CreatePartyHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private PartyService partyService;

    private static final Logger LOGGER = Logger.getLogger(CreatePartyHandler.class.getName());

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        boolean ownerControlsOnly = data.getBoolean("ownerControlsOnly");
        String token = data.getString("token");
        String videoID = data.getString("videoID");
        String roomThemeColour = data.getString("theme");

        AuthResponse tokenResponse = authService.validateToken(token);
        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String ownerUsername = tokenResponse.getActingString();
        Party party = new Party(partyService.generateRoomID(), ownerUsername, roomThemeColour, videoID, ownerControlsOnly);
        partyService.saveParty(party);
        session.sendSuccess(new JSONObject().put("partyID", party.getPartyID()), this.getHandlerType());
        LOGGER.log(Level.INFO, "Party created by " + ownerUsername + " ID: " + party.getPartyID());
    }

    @Override
    public String getHandlerType() {
        return "party-createparty";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}