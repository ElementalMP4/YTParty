package main.java.de.voidtech.ytparty.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.persistence.TokenContainer;

@Service
public class UserTokenService {
	

	private static final String LEXICON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890!£$%^&#@?";

	private static final List<String> LEXICON = Arrays.asList(LEXICON_CHARS.split(""));
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private synchronized TokenContainer getTokenContainer(String query, String mode) {

		try(Session session = sessionFactory.openSession())	{
			TokenContainer token = (TokenContainer) session.createQuery("FROM Tokens WHERE " + mode + " = :query")
                    .setParameter("query", query)
                    .uniqueResult();
			return token;
		}
		

	}
	

	private synchronized void saveToken(TokenContainer token) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();			
			session.saveOrUpdate(token);
			session.getTransaction().commit();
		}
	}
	

	public synchronized void removeToken(String username) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Tokens WHERE username = :username")
				.setParameter("username", username)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	

    private synchronized String generateToken() {
    	String token = "";
		Random random = new SecureRandom();
		for (int i = 0; i < 40; i++) token += LEXICON.get(random.nextInt(LEXICON.size() - 1));

		return token;
    }
    
	public synchronized String getToken(String username) {
		if (getTokenContainer(username, "username") == null) saveToken(new TokenContainer(username, generateToken()));
		return getTokenContainer(username, "username").getToken();
	}
	
	public synchronized String getUsernameFromToken(String token) {
		TokenContainer container = getTokenContainer(token, "token");
		return container == null ? null : container.getUsername();

	}
}