package main.java.de.voidtech.ytparty.service;

import main.java.de.voidtech.ytparty.persistence.TokenContainer;
import main.java.de.voidtech.ytparty.persistence.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class UserTokenService {

	@Autowired
	private TokenRepository tokenRepository;

	private static final String LEXICON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890!£$%^&#@?";

	private static final List<String> LEXICON = Arrays.asList(LEXICON_CHARS.split(""));

	public void removeToken(String username) {
		tokenRepository.deleteToken(username);
	}

    private String generateToken() {
    	StringBuilder token = new StringBuilder();
		Random random = new SecureRandom();
		for (int i = 0; i < 40; i++) token.append(LEXICON.get(random.nextInt(LEXICON.size() - 1)));
		return token.toString();
    }
    
	public String getToken(String username) {
		TokenContainer container = tokenRepository.getContainerByUsername(username);
		if (container == null) {
			container = new TokenContainer(username, generateToken());
			tokenRepository.save(container);
		}
		return container.getToken();
	}
	
	public String getUsernameFromToken(String token) {
		return tokenRepository.getContainerByToken(token).getUsername();
	}
}