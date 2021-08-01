package main.java.de.voidtech.ytparty.service;

import java.security.SecureRandom;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.TokenContainer;

@Service
public class UserTokenService {
	
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
        return Long.toString(Math.abs(new SecureRandom().nextLong()), 16);
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