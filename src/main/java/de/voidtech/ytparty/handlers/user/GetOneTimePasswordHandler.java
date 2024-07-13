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
public class GetOneTimePasswordHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String password = data.getString("password").trim();

        AuthResponse tokenResponse = authService.validateToken(token);

        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        if (!user.checkPassword(password)) {
            session.sendError("Your password is not correct!", this.getHandlerType());
            return;
        }

        JSONObject response = new JSONObject();
        response.put("otp", user.getOneTimePasswordCode());
        response.put("username", user.getUsername());
        session.sendSuccess(response, this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-getotp";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}