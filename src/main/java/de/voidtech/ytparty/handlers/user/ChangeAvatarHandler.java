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
public class ChangeAvatarHandler extends AbstractHandler {

    @Autowired
    private GatewayAuthService authService;

    @Autowired
    private UserService userService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String avatar = data.getString("avatar");

        AuthResponse tokenResponse = authService.validateToken(token);
        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        user.setProfilePicture(avatar);
        userService.saveUser(user);
        session.sendSuccess(new JSONObject().put("message", "Avatar changed!"), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-changeavatar";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }

}
