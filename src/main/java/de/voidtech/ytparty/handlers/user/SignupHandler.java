package main.java.de.voidtech.ytparty.handlers.user;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.CaptchaAuthService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class SignupHandler extends AbstractHandler {
	
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;
	
	@Autowired
	private CaptchaAuthService captchaService;
	
	@Override
	public void execute(GatewayConnection session, JSONObject data) {
		String username = data.getString("username");
		String password = data.getString("password");
		String passwordConfirm = data.getString("password-confirm");
		String avatar = data.getString("avatar");
		String captchaToken = data.getString("captcha-token");

		if (username.equals(""))
			responder.sendError(session, "That username is not valid!", this.getHandlerType());
		else if (username.length() > 32)
			responder.sendError(session, "That username is too long! It must be less than 32 characters.", this.getHandlerType());
		else if (!password.equals(passwordConfirm))
			responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
		else if (!PASSWORD_PATTERN.matcher(password).matches())
			responder.sendError(session, "The password you entered does not meet the complexity requirements! "
					+ "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
		else if (userService.usernameInUse(username))
			responder.sendError(session, "That username is already in use!", this.getHandlerType());
		else if (avatar.equals(""))
			responder.sendError(session, "You must choose an avatar!", this.getHandlerType());
		else if (!captchaService.validateCaptcha(captchaToken))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else {
			User newUser = new User(username, null, password, "#FF0000", avatar);
			userService.saveUser(newUser);
			responder.sendSuccess(session, new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
		}
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
