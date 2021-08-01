package main.java.de.voidtech.ytparty.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.User;

@Service
public class UserService {
	
	@Autowired
	private SessionFactory sessionFactory;
		
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
}