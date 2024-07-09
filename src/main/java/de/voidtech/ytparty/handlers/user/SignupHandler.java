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

import java.util.regex.Pattern;

@Handler
public class SignupHandler extends AbstractHandler {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");

    @Autowired
    private UserTokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaAuthService captchaService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String username = data.getString("username");
        String password = data.getString("password");
        String passwordConfirm = data.getString("password-confirm");
        String avatar = data.getString("avatar");
        String captchaToken = data.getString("captcha-token");

        if (username.isEmpty()) {
            session.sendError("That username is not valid!", this.getHandlerType());
            return;
        }
        if (username.length() > 32) {
            session.sendError("That username is too long! It must be less than 32 characters.", this.getHandlerType());
            return;
        }
        if (!password.equals(passwordConfirm)) {
            session.sendError("The passwords you entered do not match!", this.getHandlerType());
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            session.sendError("The password you entered does not meet the complexity requirements! "
                    + "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
            return;
        }
        if (userService.usernameInUse(username)) {
            session.sendError("That username is already in use!", this.getHandlerType());
            return;
        }
        if (avatar.isEmpty()) {
            session.sendError("You must choose an avatar!", this.getHandlerType());
            return;
        }
        if (!captchaService.validateCaptcha(captchaToken)) {
            session.sendError("You need to complete the captcha!", this.getHandlerType());
            return;
        }

        User newUser = new User(username, null, password, "#FF0000", avatar);
        userService.saveUser(newUser);
        session.sendSuccess(new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-signup";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }

}
