package main.java.de.voidtech.ytparty.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.persistent.TokenContainer;

@Service
public class UserTokenService {
	
	//We need a list of characters that can be used to create a token
	private static final String LEXICON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890!£$%^&#@?";
	//Put these characters into a list to make them easier to work with
	private static final List<String> LEXICON = Arrays.asList(LEXICON_CHARS.split(""));
	
	@Autowired
	private SessionFactory sessionFactory; //Inject the session factory
	
	private synchronized TokenContainer getTokenContainer(String query, String mode) {
		//We can either get a token container by username or by token. 
		try(Session session = sessionFactory.openSession())	{ //First we open a session
			TokenContainer token = (TokenContainer) session.createQuery("FROM Tokens WHERE " + mode + " = :query") //Create the query
                    .setParameter("query", query) //Set the term we are searching by
                    .uniqueResult(); //Get a single result
			return token; //Return the token container
		}
		
		//Mode will either be Username or Token (both are columns in the Tokens table)
	}
	
	//Take a TokenContainer and persist it to the database
	private synchronized void saveToken(TokenContainer token) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();			
			session.saveOrUpdate(token);
			session.getTransaction().commit();
		}
	}
	
	//Remove a persisted token container
	public synchronized void removeToken(String username) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Tokens WHERE username = :username")
				.setParameter("username", username)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	//Generate a 40 character token string
    private synchronized String generateToken() {
    	String token = ""; //Create a string to put the token inside
		Random random = new SecureRandom(); //Create a new randomiser
		for (int i = 0; i < 40; i++) token += LEXICON.get(random.nextInt(LEXICON.size() - 1)); //Repeat this step 40 times.
		//Choose a random character to be appended to the token 
		return token; //return the token
    }
    
	public synchronized String getToken(String username) { //This method will automatically create a new token if one is not already present
		if (getTokenContainer(username, "username") == null) saveToken(new TokenContainer(username, generateToken()));
		return getTokenContainer(username, "username").getToken();
	}
	
	public synchronized String getUsernameFromToken(String token) {
		TokenContainer container = getTokenContainer(token, "token");
		return container == null ? null : container.getUsername();
		//If there is no token for this username, then return null. Otherwise get the username stored in the token container 
	}
}