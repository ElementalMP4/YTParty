package main.java.de.voidtech.ytparty.handlers.user;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.PasswordResetCase;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.MailService;
import main.java.de.voidtech.ytparty.service.PasswordResetService;
import main.java.de.voidtech.ytparty.service.UserService;

@Handler
public class PasswordForgottenHandler extends AbstractHandler {
	
	private static final String RESET_URL = "https://ytparty.voidtech.de/resetpassword.html";

	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordResetService passwordService;
	
	@Autowired
	private CaptchaAuthService captchaService;
	
	@Override
	public void execute(WebSocketSession session, JSONObject data) {
		String username = data.getString("username");
		String captchaToken = data.getString("captcha-token");
		User user = userService.getUser(username);
		
		if (!captchaService.validateCaptcha(captchaToken))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else if (user == null)
			responder.sendError(session, "That username does not exist!", this.getHandlerType());
		else {
			String email = user.getEmail();
			if (email == null) responder.sendError(session, "This account has no recovery email!", this.getHandlerType());
			else {
				String title = "Password reset request";
				PasswordResetCase resetCase = passwordService.openPasswordResetCase(username);
				String body = "You have requested to reset your password. Please click the link below to continue:\n\n" +
				RESET_URL + "?token=" + resetCase.getToken() + "&user=" + username +
					"\n\nKeep this link a secret! Someone could use it to take over your account.";
				mailService.sendMessage(email, body, title);
				responder.sendSuccess(session, "Please check your e-mail inbox. Please also check your spam if you cannot find our message.", this.getHandlerType());	
			}
		}
	}

	@Override
	public String getHandlerType() {
		return "user-forgottenpassword";
	}
	
	@Override
	public boolean requiresRateLimit() {
		return true;
	}

}
