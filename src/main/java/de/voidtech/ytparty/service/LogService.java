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
	
	private static final String AVATAR_URL = "https://ytparty.voidtech.de/favicon.png";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
	private static final Logger LOGGER = Logger.getLogger(LogService.class.getName());
	
	public void sendNotification(String message) {
		String webhookUrl = configService.getLogWebhookURL();
		if (webhookUrl == null) return;
		JSONObject webhookPayload = new JSONObject();
        webhookPayload.put("content", message);
        webhookPayload.put("username", "YTParty Status");
        webhookPayload.put("avatar_url", AVATAR_URL);
        webhookPayload.put("tts", false);
        try {              			
        	
            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "YTParty");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream stream = connection.getOutputStream();
            stream.write(webhookPayload.toString().getBytes());
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();
        	
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
            ex.printStackTrace();
        }
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void initialise() {
		alertProgramStarted();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            alertProgramShutdown();
	        }
	    }, "Shutdown Alert"));
	}
	
	public void alertProgramStarted() {
		sendNotification("YTParty started at " + formatter.format(new Date()));
	}
	
	public void alertProgramShutdown() {
		sendNotification("YTParty shut down at " + formatter.format(new Date()));
	}
}
