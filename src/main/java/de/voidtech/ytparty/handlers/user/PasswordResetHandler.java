package main.java.de.voidtech.ytparty.handlers.user;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.PasswordResetService;
import main.java.de.voidtech.ytparty.service.UserService;
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

    @Autowired
    private PasswordResetService passwordService;

    @Override
    public void execute(GatewayConnection session, JSONObject data) {
        String resetToken = data.getString("reset-token");
        String password = data.getString("password");
        String passwordRepeat = data.getString("password-confirm");
        String captchaToken = data.getString("captcha-token");
        String username = data.getString("username");

        if (passwordService.getCaseFromResetToken(resetToken) == null) {
            session.sendError("This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
            return;
        }
        if (!passwordService.getCaseFromResetToken(resetToken).getUser().equals(username)) {
            session.sendError("This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
            return;
        }
        if (!password.equals(passwordRepeat)) {
            session.sendError("The passwords you entered do not match!", this.getHandlerType());
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            session.sendError("The password you entered does not meet the complexity requirements! "
                    + "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
            return;
        }
        if (!captchaService.validateCaptcha(captchaToken)) {
            session.sendError("You need to complete the captcha!", this.getHandlerType());
            return;
        }

        User user = userService.getUser(username);
        user.setPassword(password);
        userService.saveUser(user);
        passwordService.closePasswordCase(passwordService.getCaseFromResetToken(resetToken));
        session.sendSuccess(new JSONObject().put("message", "Password reset successfully!"), this.getHandlerType());
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
