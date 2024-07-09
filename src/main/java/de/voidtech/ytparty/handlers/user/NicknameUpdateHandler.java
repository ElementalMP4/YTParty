package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class NicknameUpdateHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String nickname = data.getString("nickname").trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        String token = data.getString("token");
        AuthResponse tokenResponse = authService.validateToken(token);

        if (nickname.equals("") || nickname.length() > 32) {
            session.sendError("That nickname is too long! It must be less than 32 characters.!", this.getHandlerType());
            return;
        }
        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        user.setNickname(nickname);
        userService.saveUser(user);
        session.sendSuccess(new JSONObject().put("message", "Nickname changed!"), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-changenickname";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}