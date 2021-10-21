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
	public void execute(WebSocketSession session, JSONObject data) {
		
		if (data.getString("username").equals(""))
			responder.sendError(session, "That username is not valid!", this.getHandlerType());
		else if (!data.getString("password").equals(data.get("password-confirm")))
			responder.sendError(session, "The passwords you entered do not match!", this.getHandlerType());
		else if (!PASSWORD_PATTERN.matcher(data.getString("password")).matches())
			responder.sendError(session, "The password you entered does not meet the complexity requirements! "
					+ "(One capital letter, One number, 8 Characters long)", this.getHandlerType());
		else if (userService.usernameInUse(data.getString("username")))
			responder.sendError(session, "That username is already in use!", this.getHandlerType());
		else if (data.getString("email").equals("")) 
			responder.sendError(session, "You must enter an email address", this.getHandlerType());
		else if (!captchaService.validateCaptcha(data.getString("captcha-token")))
			responder.sendError(session, "You need to complete the captcha!", this.getHandlerType());
		else {
			User newUser = new User(data.getString("username"), null, data.getString("password"), "#FF0000", data.getString("email"));
			userService.saveUser(newUser);
			responder.sendSuccess(session, tokenService.getToken(data.getString("username")), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-signup";
	}

}
