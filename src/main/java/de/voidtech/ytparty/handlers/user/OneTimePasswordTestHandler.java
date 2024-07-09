package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.AuthResponse;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.GatewayAuthService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.utils.TOTPUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class OneTimePasswordTestHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayAuthService authService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String oneTimePassword = data.getString("otp").replaceAll("[^0-9]", "");
        String token = data.getString("token");
        AuthResponse tokenResponse = authService.validateToken(token);

        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        String generated = TOTPUtils.generateCode(user.getOneTimePasswordCode());
        boolean correct = generated.equals(oneTimePassword);
        session.sendSuccess(new JSONObject().put("match", correct).put("received", oneTimePassword).put("generated", generated), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-testotp";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }

}