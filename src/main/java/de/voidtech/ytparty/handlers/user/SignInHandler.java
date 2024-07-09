package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Handler
public class SignInHandler extends AbstractHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTokenService tokenService;

    @Autowired
    private CaptchaAuthService captchaService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String username = data.getString("username");
        String enteredPassword = data.getString("password");
        String captchaToken = data.getString("captcha-token");
        User user = userService.getUser(username);

        if (!captchaService.validateCaptcha(captchaToken)) {
            session.sendError("You need to pass the captcha!", this.getHandlerType());
            return;
        }
        if (user == null) {
            session.sendError("Username or Password incorrect", this.getHandlerType());
            return;
        }

        if (!user.checkPassword(enteredPassword)) {
            session.sendError("Username or Password incorrect", this.getHandlerType());
            return;
        }
        session.sendSuccess(new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-signin";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }
}