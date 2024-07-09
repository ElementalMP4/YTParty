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

import java.util.Set;

@Handler
public class TypingUpdateHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private PartyService partyService;

    private static final Set<String> VALID_MODES = Set.of("start", "stop");

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String roomID = data.getString("roomID");
        String mode = data.getString("mode");
        String user = data.getString("user");

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
        if (!VALID_MODES.contains(mode)) {
            session.sendError("An invalid typing mode was provided", this.getHandlerType());
            return;
        }

        partyService.sendSystemMessage(party,
                new MessageBuilder()
                        .type("party-typingupdate")
                        .data(new JSONObject().put("mode", mode).put("user", user))
                        .buildToSystemMessage());
    }

    @Override
    public String getHandlerType() {
        return "party-typingupdate";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }
}