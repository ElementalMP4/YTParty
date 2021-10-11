package main.java.de.voidtech.ytparty.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaptchaAuthService {
	
	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";	
	private static final Logger LOGGER = Logger.getLogger(CaptchaAuthService.class.getName());
	
	@Autowired
	private ConfigService configService;
	
	public boolean validateCaptcha(String response) {
	    try {
	        String params = "secret=" + configService.getHCaptchaToken() + "&response=" + response;

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
	        
	        LOGGER.log(Level.INFO, "Submitted reCaptcha, returned " + (responseJson.getBoolean("success") ? "success" : "failure"));

	        return responseJson.getBoolean("success");
	    } catch (Exception e) {
	        LOGGER.log(Level.SEVERE, "An error occurred during ServiceExecution: " + e.getMessage());
	    }
	    return false;
	}
}
