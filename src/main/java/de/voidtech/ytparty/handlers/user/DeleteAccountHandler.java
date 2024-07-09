package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class DeleteAccountHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTokenService tokenService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String password = data.getString("password");

        AuthResponse tokenResponse = authService.validateToken(token);
        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        if (!user.checkPassword(password)) {
            session.sendError("The password you entered is not correct!", this.getHandlerType());
            return;
        }

        tokenService.removeToken(username);
        userService.removeUser(username);
        session.sendSuccess("Account deleted!", this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-deleteaccount";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }

}