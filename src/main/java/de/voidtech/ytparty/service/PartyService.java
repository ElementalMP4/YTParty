package main.java.de.voidtech.ytparty.service;

import main.java.de.voidtech.ytparty.entities.GatewayConnection;
import main.java.de.voidtech.ytparty.entities.Party;
import main.java.de.voidtech.ytparty.entities.SystemMessage;
import main.java.de.voidtech.ytparty.persistence.ChatMessage;
import main.java.de.voidtech.ytparty.persistence.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PartyService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final List<String> LEXICON = Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890".split(""));

    private final HashMap<String, Party> parties = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void cleanDatabase() {
        chatMessageRepository.clean();
    }

    public void deletePartyMessageHistory(String partyID) {
        chatMessageRepository.clearMessageHistory(partyID);
    }

    public String generateRoomID() {
        StringBuilder ID = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) ID.append(LEXICON.get(random.nextInt(LEXICON.size() - 1)));
        return ID.toString();
    }

    public Party getParty(String partyID) {
        return parties.get(partyID);
    }

    public void saveParty(Party party) {
        parties.put(party.getPartyID(), party);
    }

    public void removeSessionFromParty(GatewayConnection session) {
        List<String> invalidParties = new ArrayList<>();
        for (String key : parties.keySet()) {
            Party party = parties.get(key);
            party.checkRemoveSession(session);
            if (party.getAllSessions().isEmpty() && party.hasBeenVisited()) invalidParties.add(key);
        }
        for (String key : invalidParties) {
			String partyID = parties.get(key).getPartyID();
			deletePartyMessageHistory(partyID);
			parties.remove(key);
		}
    }

    public List<ChatMessage> getMessageHistory(String roomID) {
        return chatMessageRepository.getMessageHistory(roomID);
    }

    public void sendChatMessage(Party party, ChatMessage message) {
        party.broadcastMessage(message);
        chatMessageRepository.save(message);
    }

    public void sendSystemMessage(Party party, SystemMessage systemMessage) {
        party.broadcastMessage(systemMessage);
    }
}