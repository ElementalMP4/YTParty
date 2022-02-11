package main.java.de.voidtech.ytparty.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.ephemeral.Party;

@Service
public class PartyService {
	
	//We create a list of characters which can be used to generate a room ID
	private static final List<String> LEXICON = Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890".split(""));
	//We store the parties in a hashmap, which is a very efficient way of accessing a large number of items.
	private HashMap<String, Party> parties = new HashMap<String, Party>();  
	
	@Autowired
	private SessionFactory sessionFactory; //We need the database session factory so we can perform SQL operations
	
	@EventListener(ApplicationReadyEvent.class) //This annotation tells spring to run this method when the program is ready
	private void cleanDatabase() {
		try(Session session = sessionFactory.openSession())	{ //Create a new database session
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Messages") //Delete ALL messages from the message history
				.executeUpdate(); //Execute the statement
			session.getTransaction().commit(); //Save the database changes
		}
	}
	
	public void deletePartyMessageHistory(String partyID) { //This method is used to clear the history of a single party
		try(Session session = sessionFactory.openSession())	{ //Open a session
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Messages where partyID = :partyID") //Delete all messages from a single party
				.setParameter("partyID", partyID) //Set the party ID parameter in the HQL statement
				.executeUpdate(); //Execute the statement
			session.getTransaction().commit(); //Save the database changes
		}
	}
	
	public String generateRoomID() {
		String ID = ""; //Create a string to store the ID
		Random random = new Random(); //Create a new randomiser
		for (int i = 0; i < 8; i++) ID += LEXICON.get(random.nextInt(LEXICON.size() - 1)); //Create an 8-character long ID
		return ID; //return this ID
	}
	
	public synchronized Party getParty(String partyID) {
		return parties.get(partyID); //Get a party by ID
	}
	
	public synchronized void saveParty(Party party) {
		parties.put(party.getPartyID(), party); //Save a new party
	}
	
	public synchronized void deleteParty(String partyID) {
		parties.remove(partyID); //Remove a party by ID
	}
	
	public synchronized void removeSessionFromParty(GatewayConnection session) { //Remove someone from a party when they disconnect
		List<String> invalidParties = new ArrayList<String>(); //Create a list of empty parties
		for (String key : parties.keySet()) { //Iterate through every party
			Party party = parties.get(key); //Get a party by ID
			party.checkRemoveSession(session); //Check the party to see if this session is within it
			if (party.getAllSessions().isEmpty() && party.hasBeenVisited()) invalidParties.add(key); //If the party is empty, invalidate it.
		}
		if (!invalidParties.isEmpty()) for (String key : invalidParties) { //If we have empty parties, iterate through the list
			String partyID = parties.get(key).getPartyID(); //Get the party by ID
			deletePartyMessageHistory(partyID); //Delete its message history
			parties.remove(key); //Close the party
		};
	}
	
	public int getPartyCount() {
		return parties.size(); //Get the number of active parties
	}
}