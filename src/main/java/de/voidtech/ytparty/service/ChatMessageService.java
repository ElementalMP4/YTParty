package main.java.de.voidtech.ytparty.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.persistent.ChatMessage;

@Service
public class ChatMessageService {

	@Autowired
	private SessionFactory sessionFactory; //Get the database session factory
	
	public void saveMessage(ChatMessage message) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(message); //Save a message to the database
			session.getTransaction().commit();
		}
	}
	
	@SuppressWarnings("unchecked") //Hibernate cannot ensure the type of the entities it is returning.
	//We can tell java to ignore this warning as we will only be returning ChatMessage objects. 
	public List<ChatMessage> getMessageHistory(String partyID) {
		try(Session session = sessionFactory.openSession()) //Create a session
		{
			List<ChatMessage> messages = (List<ChatMessage>) session.createQuery("FROM Messages WHERE partyID = :partyID")
					//Get all the messages from a party
                    .setParameter("partyID", partyID) //Set the party ID parameter
                    .list(); //Return a list of entities, not a singular entity.
			return (List<ChatMessage>) messages; //return this list to the calling method.
		}
	}
}
