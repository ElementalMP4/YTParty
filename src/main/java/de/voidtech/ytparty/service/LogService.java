package main.java.de.voidtech.ytparty.service;

import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class LogService {

	@Autowired
	private ConfigService configService;
	
	private static final String AVATAR_URL = "https://ytparty.voidtech.de/favicon.png"; //Set the icon of the webhook message
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //Create a new date/time formatter
	private static final Logger LOGGER = Logger.getLogger(LogService.class.getName()); //Get the internal logger
	
	public void sendNotification(String message) { //Send a message via the webhook URL from the config file
		String webhookUrl = configService.getLogWebhookURL(); //Get the URL
		if (webhookUrl == null) return; //If it has not been set, then we will send nothing
		JSONObject webhookPayload = new JSONObject(); //Create a new JSON payload
        webhookPayload.put("content", message); //Set the message content
        webhookPayload.put("username", "YTParty Status"); //Set the name of the webhook
        webhookPayload.put("avatar_url", AVATAR_URL); //Set the avatar URL
        webhookPayload.put("tts", false); //Disable text-to-speech
        
        try {              			
            URL url = new URL(webhookUrl); //Create a new URL object
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); //Open an HTTPS connection for this URL
            
            connection.addRequestProperty("Content-Type", "application/json"); //Set the content MIME type
            connection.addRequestProperty("User-Agent", "YTParty"); //Set the user agent (not required, but good practice)
            connection.setDoOutput(true); //We will be streaming data to this address, so we need to enable output
            connection.setRequestMethod("POST"); //Set the content type to POST (we are creating new data on discord's servers)

            OutputStream stream = connection.getOutputStream(); //Open the output stream
            stream.write(webhookPayload.toString().getBytes()); //Write the bytes of the payload
            stream.flush(); //Flush the output and ensure all data has been sent
            stream.close(); //Close the stream

            connection.getInputStream().close(); //Close the response stream
            connection.disconnect(); //Close the connection
        	
        } catch (Exception ex) {
        	//Log any errors with the message sending
            LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
        }
	}
	
	@EventListener(ApplicationReadyEvent.class) //This annotation tells spring to run this method when the program has loaded successfully
	public void initialise() {
		alertProgramStarted(); //Send the startup alert
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() { //Add a new shutdown hook...
	        public void run() {
	            alertProgramShutdown(); //...which will alert us when the program stops
	        }
	    }, "Shutdown Alert")); //Name this thread
	}
	
	public void alertProgramStarted() { //Send the startup message and timestamp
		sendNotification("YTParty started at " + formatter.format(new Date()));
	}
	
	public void alertProgramShutdown() { //Send the shutdown message and timestamp
		sendNotification("YTParty shut down at " + formatter.format(new Date()));
	}
}
