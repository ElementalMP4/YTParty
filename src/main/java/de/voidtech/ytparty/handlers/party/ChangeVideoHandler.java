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
public class ChangeVideoHandler extends AbstractHandler {

    @Autowired
    private PartyService partyService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String roomID = data.getString("roomID");
        String newVideoID = data.getString("video");

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
            session.sendError("You're not allowed to change the video!", this.getHandlerType());
            return;
        }

        ChatMessage videoMessage = new MessageBuilder()
                .partyID(roomID)
                .author(MessageBuilder.SYSTEM_AUTHOR)
                .colour(party.getRoomColour())
                .content(String.format("Video Changed by %s!", tokenResponse.getActingString()))
                .avatar(MessageBuilder.SYSTEM_AVATAR)
                .buildToChatMessage();

        party.setVideoID(newVideoID);
        partyService.sendChatMessage(party, videoMessage);
        partyService.sendSystemMessage(party, new MessageBuilder()
                .type("party-changevideo")
                .data(new JSONObject().put("video", newVideoID))
                .buildToSystemMessage());
    }

    @Override
    public String getHandlerType() {
        return "party-changevideo";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }
}