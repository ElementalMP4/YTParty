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
public class ChatMessageHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private PartyService partyService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String roomID = data.getString("roomID");
        String content = data.getString("content").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        String colour = data.getString("colour");
        String author = data.getString("author");
        String avatar = data.getString("avatar");

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
        if (content.length() > 2000) {
            session.sendError("Your message is too long! Messages cannot be longer than 2000 characters.",
                    this.getHandlerType());
            return;
        }
        ChatMessage userMessage = new MessageBuilder()
                .partyID(roomID)
                .content(content)
                .colour(colour)
                .author(author)
                .avatar(avatar)
                .buildToChatMessage();
        partyService.sendChatMessage(party, userMessage);
    }

    @Override
    public String getHandlerType() {
        return "party-chatmessage";
    }

    @Override
    public boolean requiresRateLimit() {
        return false;
    }
}