package main.java.de.voidtech.ytparty.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.persistent.User;

@Service
public class UserService {
	
	@Autowired
	private SessionFactory sessionFactory; //Inject the session factory from DatabaseService
		
	public synchronized boolean usernameInUse(String username) {
		return getUser(username) != null; //A method to see if a username is already in use
	}
	
	public synchronized User getUser(String username) {
		try(Session session = sessionFactory.openSession()) //Create a new querying session
		{
			//Hibernate will automatically put the results into a "User" class
			User user = (User) session.createQuery("FROM Users WHERE username =:username") //Select the user by username
                    .setParameter("username", username) //Set the username in the query to the given username
                    .uniqueResult(); //We want a single result. An error will occur if it is not unique.
			return user; //Return the user
		}
	}
	
	public synchronized void saveUser(User user) {
		try(Session session = sessionFactory.openSession()) //Create a new querying session
		{
			session.getTransaction().begin(); //Begin a database transaction
			session.saveOrUpdate(user); //The class will automatically be analysed and a SQL statement constructed
			session.getTransaction().commit(); //The transaction is committed to the database and closed.
		}
	}
	
	public synchronized void removeUser(String username) {
		try(Session session = sessionFactory.openSession())	{ //Create a new querying session
			session.getTransaction().begin(); //Begin a transaction
			session.createQuery("DELETE FROM Users WHERE username = :username") //Remove the user by username
				.setParameter("username", username) //Set the username in the query to the given username
				.executeUpdate(); //Tell hibernate that we will not be expecting a result, and to only execute this query.
			session.getTransaction().commit(); //Commit the changes
		}
	}
}