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
public class ColourChangeHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String colour = data.getString("colour");
        String token = data.getString("token");

        AuthResponse tokenResponse = authService.validateToken(token);
        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        user.setHexColour(colour);
        userService.saveUser(user);
        session.sendSuccess(new JSONObject().put("message", "Colour changed!"), this.getHandlerType());

    }

    @Override
    public String getHandlerType() {
        return "user-changecolour";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}