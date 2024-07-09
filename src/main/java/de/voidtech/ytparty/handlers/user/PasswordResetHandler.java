package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.utils.TOTPUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

@Handler
public class PasswordResetHandler extends AbstractHandler {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");

    @Autowired
    private CaptchaAuthService captchaService;

    @Autowired
    private UserService userService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String oneTimePassword = data.getString("otp").replaceAll("[^0-9]", "");
        String password = data.getString("password");
        String passwordRepeat = data.getString("password-confirm");
        String captchaToken = data.getString("captcha-token");
        String username = data.getString("username");

        if (!password.equals(passwordRepeat)) {
            session.sendError("The passwords you entered do not match!", this.getHandlerType());
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            session.sendError("The password you entered does not meet the complexity requirements! "
                    + "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
            return;
        }
        User user = userService.getUser(username);
        if (!TOTPUtils.generateCode(user.getOneTimePasswordCode()).equals(oneTimePassword)) {
            session.sendError("OTP is incorrect", this.getHandlerType());
            return;
        }
        if (!captchaService.validateCaptcha(captchaToken)) {
            session.sendError("You need to complete the captcha!", this.getHandlerType());
            return;
        }

        user.setPassword(password);
        userService.saveUser(user);
        session.sendSuccess("Password reset successfully!", this.getHandlerType());
    }

    @Override
    public String getHandlerType() {
        return "user-resetpassword";
    }

    @Override
    public boolean requiresRateLimit() {
        return true;
    }

}
