package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.PasswordResetService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class PasswordResetHandler extends AbstractHandler {
	
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private CaptchaAuthService captchaService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private PasswordResetService passwordService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String resetToken = data.getString("reset-token");
		String password = data.getString("password");
		String passwordRepeat = data.getString("password-confirm");	
		String captchaToken = data.getString("captcha-token");
		String username = data.getString("username");
		
		if (passwordService.getCaseFromResetToken(resetToken) == null)
			responder.sendError(session, "This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
		else if (!passwordService.getCaseFromResetToken(resetToken).getUser().equals(username))
			responder.sendError(session, "This reset token is not valid! It may be incorrect or it may have expired.", this.getHandlerType());
		if (!password.equals(passwordRepeat))
			responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
		else if (!PASSWORD_PATTERN.matcher(password).matches())
			responder.sendError(session, "The password you entered does not meet the complexity requirements! "
					+ "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
		else if (!captchaService.validateCaptcha(captchaToken))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else {
			User user = userService.getUser(username);
			user.setPassword(password);
			userService.saveUser(user);
			passwordService.closePasswordCase(passwordService.getCaseFromResetToken(resetToken));
			responder.sendSuccess(session, "Password reset successfully!", this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-resetpassword";
	}

}
