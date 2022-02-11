package main.java.de.voidtech.ytparty.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaptchaAuthService {
	
	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
	//We need to know where to send our tokens to be authenticated
	private static final Logger LOGGER = Logger.getLogger(CaptchaAuthService.class.getName());
	//Log all captcha requests, we should know if a request fails. Additionally, we need to see errors from this service.
	
	@Autowired
	private ConfigService configService; //We need our secret reCaptcha key to validate the requests.
	
	public boolean validateCaptcha(String response) {
	    try {
	        String params = "secret=" + configService.getCaptchaToken() + "&response=" + response; //Create the request body

	        HttpsURLConnection con = (HttpsURLConnection) new URL(VERIFY_URL).openConnection(); //Create a new secure connection
	        con.setDoOutput(true);
	        //We will be outputting data
	        con.setRequestMethod("POST");
	        //We are POSTing data (Post is an HTTP Request method)
	        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	        //Tell recaptcha the format of our request
	        OutputStream outStream = con.getOutputStream();
	        //Get the output stream from the requests
	        outStream.write(params.getBytes("UTF-8"));
	        //Write our data to it
	        outStream.flush();
	        //Clear the byte buffer
	        outStream.close();
	        //Close the output stream, complete the request.

	        InputStream inStream = con.getInputStream();
	        //Get the response stream
	        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
	        //Create a new buffer to receive the stream data
	        
	        StringBuilder responseString = new StringBuilder(); //We need somewhere to put the response
	        int charBuffer; //We will read each byte from the buffer. If the byte is -1, we have reached the end of the buffer.
	        while ((charBuffer = buffer.read()) != -1) {
	            responseString.append((char) charBuffer); //Add each character to the resposne string.
	        }
	        JSONObject responseJson = new JSONObject(responseString.toString()); //Parse the response to JSON
	        inStream.close(); //Close the input stream
	        
	        LOGGER.log(Level.INFO, "Submitted reCaptcha, returned " + (responseJson.getBoolean("success") ? "success" : "failure"));
	        //Log whether the captcha was a success
	        
	        return responseJson.getBoolean("success");
	        //Return this value to the handler that asked for it
	    } catch (Exception e) {
	        LOGGER.log(Level.SEVERE, "An error occurred during ServiceExecution: " + e.getMessage());
	    }
	    return false; //If there are any issues, we should always reject the request. 
	}
}
