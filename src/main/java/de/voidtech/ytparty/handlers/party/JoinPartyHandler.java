package main.java.de.voidtech.ytparty.handlers.party;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.MessageBuilder;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.ChatMessage;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.service.SessionService;
import main.java.de.voidtech.ytparty.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Handler
public class JoinPartyHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private PartyService partyService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

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

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);

        String potentialRoom = sessionService.getSessionRoomIDifExists(username);

        if (potentialRoom == null)
            joinParty(session, roomID, user);
        else if (potentialRoom.equals(roomID))
            joinParty(session, roomID, user);
        else
            session.sendError("You are already in a room! If you have connection issues, try restarting your browser.", this.getHandlerType());
    }

    private void joinParty(GatewayConnection session, String roomID, User user) {
        Party party = partyService.getParty(roomID);
        session.setName(user.getUsername());
        session.setRoomID(roomID);
        session.sendSuccess(new JSONObject()
                .put("video", party.getVideoID())
                .put("canControl", party.canControlRoom(user.getUsername()))
                .put("theme", party.getRoomColour())
                .put("owner", party.getOwnerName()), this.getHandlerType());

        ChatMessage joinMessage = new MessageBuilder()
                .partyID(roomID)
                .author(MessageBuilder.SYSTEM_AUTHOR)
                .colour(party.getRoomColour())
                .content(String.format("%s has joined the party!", user.getUsername()))
                .avatar(MessageBuilder.SYSTEM_AVATAR)
                .buildToChatMessage();
        party.addToSessions(session);
        deliverMessageHistory(session, roomID);
        partyService.sendChatMessage(party, joinMessage);
    }

    private void deliverMessageHistory(GatewayConnection session, String roomID) {
        List<ChatMessage> messageHistory = partyService.getMessageHistory(roomID);
        session.sendChatHistory(messageHistory);
    }

    @Override
    public String getHandlerType() {
        return "party-joinparty";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}