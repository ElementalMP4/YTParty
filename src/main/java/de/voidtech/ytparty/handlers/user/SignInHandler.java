package main.java.de.voidtech.ytparty.handlers.user;

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
public class SignInHandler extends AbstractHandler {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;

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
		
		if (captchaService.validateCaptcha(captchaToken)) {
			if (user == null) responder.sendError(session, "Username or Password incorrect", this.getHandlerType());
			else {
				if (user.checkPassword(enteredPassword)) responder.sendSuccess(session, new JSONObject().put("token", tokenService.getToken(username)), this.getHandlerType());
				else responder.sendError(session, "Username or Password incorrect", this.getHandlerType());
			}	
		} else responder.sendError(session, "You need to pass the captcha!", this.getHandlerType());
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