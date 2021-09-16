package main.java.de.voidtech.ytparty.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.User;

@Service
public class UserService {
	
	private static final String VERIFY_URL = "https://hcaptcha.com/siteverify";	
	private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
	private static final Pattern passwordPattern = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}");
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private UserTokenService tokenService;
	
	@Autowired
	private ConfigService configService;
		
	public synchronized boolean usernameInUse(String username) {
		return getUser(username) != null;
	}
	
	public synchronized User getUser(String username) {
		try(Session session = sessionFactory.openSession())
		{
			User user = (User) session.createQuery("FROM Users WHERE username =:username")
                    .setParameter("username", username)
                    .uniqueResult();
			return user;
		}
	}
	
	public synchronized void saveUser(User user) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(user);
			session.getTransaction().commit();
		}
	}
	
	public synchronized void removeUser(String username) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Users WHERE username = :username")
				.setParameter("username", username)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	private boolean getCaptchaResponse(String secretKey, String response) {
	    try {
	        String url = VERIFY_URL, params = "secret=" + secretKey + "&response=" + response;

	        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
	        http.setDoOutput(true);
	        http.setRequestMethod("POST");
	        http.setRequestProperty("Content-Type",
	                "application/x-www-form-urlencoded; charset=UTF-8");
	        OutputStream out = http.getOutputStream();
	        out.write(params.getBytes("UTF-8"));
	        out.flush();
	        out.close();

	        InputStream res = http.getInputStream();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(res, "UTF-8"));

	        StringBuilder sb = new StringBuilder();
	        int cp;
	        while ((cp = rd.read()) != -1) {
	            sb.append((char) cp);
	        }
	        JSONObject json = new JSONObject(sb.toString());
	        res.close();

	        return json.getBoolean("success");
	    } catch (Exception e) {
	        LOGGER.log(Level.SEVERE, "An error occurred during ServiceExecution: " + e.getMessage());
	    }
	    return false;
	}
	
	public String createUser(JSONObject parameters) {
		if (!getCaptchaResponse(configService.getHCaptchaToken(), parameters.getString("h-captcha")))
			return new JSONObject().put("success", false).put("message", "You did not pass the captcha!").toString();
		
		else if (parameters.getString("username").equals(""))
			return new JSONObject().put("success", false).put("message", "That username is not valid!").toString();
		
		if (!parameters.getString("password").equals(parameters.get("password-confirm")))
			return new JSONObject().put("success", false).put("message", "The passwords you entered do not match!").toString();
		
		else if (!passwordPattern.matcher(parameters.getString("password")).matches())
			return new JSONObject().put("success", false)
					.put("message", "The password you entered does not meet the complexity requirements! (One capital letter, One number)").toString();
		
		else if (usernameInUse(parameters.getString("username")))
			return new JSONObject().put("success", false).put("message", "That username is already in use!").toString();
		
		else {
			User newUser = new User(parameters.getString("username"), null, parameters.getString("password"), "#FF0000");
			saveUser(newUser);
			return new JSONObject().put("success", true).put("token", tokenService.getToken(parameters.getString("username"))).toString();
		}
	}
}