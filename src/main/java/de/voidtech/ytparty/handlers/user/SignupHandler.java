package main.java.de.voidtech.ytparty.handlers.user;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import main.java.de.voidtech.ytparty.annotations.Handler;
import main.java.de.voidtech.ytparty.entities.persistent.User;
import main.java.de.voidtech.ytparty.handlers.AbstractHandler;
import main.java.de.voidtech.ytparty.service.ConfigService;
import main.java.de.voidtech.ytparty.service.GatewayResponseService;
import main.java.de.voidtech.ytparty.service.UserService;
import main.java.de.voidtech.ytparty.service.UserTokenService;

@Handler
public class SignupHandler extends AbstractHandler {
	
	private static final String VERIFY_URL = "https://hcaptcha.com/siteverify";	
	private static final Logger LOGGER = Logger.getLogger(SignupHandler.class.getName());
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");

	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GatewayResponseService responder;
	
	private boolean getCaptchaResponse(String secretKey, String response) {
	    try {
	        String params = "secret=" + secretKey + "&response=" + response;

	        HttpURLConnection con = (HttpURLConnection) new URL(VERIFY_URL).openConnection();
	        con.setDoOutput(true);
	        con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type",
	                "application/x-www-form-urlencoded; charset=UTF-8");
	        OutputStream outStream = con.getOutputStream();
	        outStream.write(params.getBytes("UTF-8"));
	        outStream.flush();
	        outStream.close();

	        InputStream inStream = con.getInputStream();
	        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

	        StringBuilder responseString = new StringBuilder();
	        int charBuffer;
	        while ((charBuffer = buffer.read()) != -1) {
	            responseString.append((char) charBuffer);
	        }
	        JSONObject responseJson = new JSONObject(responseString.toString());
	        inStream.close();

	        return responseJson.getBoolean("success");
	    } catch (Exception e) {
	        LOGGER.log(Level.SEVERE, "An error occurred during ServiceExecution: " + e.getMessage());
	    }
	    return false;
	}
	
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
		else if (!getCaptchaResponse(configService.getHCaptchaToken(), data.getString("h-captcha")))
			responder.sendError(session, "You did not pass the captcha!", this.getHandlerType());
		else {
			User newUser = new User(data.getString("username"), null, data.getString("password"), "#FF0000");
			userService.saveUser(newUser);
			responder.sendSuccess(session, tokenService.getToken(data.getString("username")), this.getHandlerType());
		}
	}

	@Override
	public String getHandlerType() {
		return "user-signup";
	}

}
