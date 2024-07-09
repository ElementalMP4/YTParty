package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.ChatMessage;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class ClearQueueHandler extends AbstractHandler {

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
        if (!party.canControlRoom(tokenResponse.getActingString())) {
            session.sendError("You're not allowed to clear the queue!", this.getHandlerType());
            return;
        }

        party.clearQueue();
        ChatMessage clearMessage = new MessageBuilder()
                .partyID(roomID)
                .author(MessageBuilder.SYSTEM_AUTHOR)
                .colour(party.getRoomColour())
                .content(String.format("Queue cleared by %s!", tokenResponse.getActingString()))
                .avatar(MessageBuilder.SYSTEM_AVATAR)
                .buildToChatMessage();
        partyService.sendChatMessage(party, clearMessage);
    }

    @Override
    public String getHandlerType() {
        return "party-clearqueue";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }
}