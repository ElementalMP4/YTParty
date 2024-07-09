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

import java.util.regex.Pattern;

@Handler
public class PasswordUpdateHandler extends AbstractHandler {

    @Autowired
    private UserTokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayAuthService authService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String token = data.getString("token");
        String currentPassword = data.getString("original-password").trim();
        String newPassword = data.getString("new-password").trim();
        String newPasswordConfirm = data.getString("password-match").trim();

        AuthResponse tokenResponse = authService.validateToken(token);

        if (!tokenResponse.isSuccessful()) {
            session.sendError(tokenResponse.getMessage(), this.getHandlerType());
            return;
        }

        String username = tokenResponse.getActingString();
        User user = userService.getUser(username);
        if (!user.checkPassword(currentPassword)) {
            session.sendError("Your current password is not correct!", this.getHandlerType());
            return;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            session.sendError("The passwords you entered do not match!", this.getHandlerType());
            return;
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            session.sendError("That password is not valid! Make sure it contains a capital letter and"
                    + " a number and is at least 8 characters!", this.getHandlerType());
            return;
        }

        user.setPassword(newPassword);
        userService.saveUser(user);
        tokenService.removeToken(username);
        String newToken = tokenService.getToken(username);
        session.sendSuccess(new JSONObject().put("token", newToken), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-changepassword";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}