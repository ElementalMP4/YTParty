package main.java.de.voidtech.ytparty.service;

import main.java.de.voidtech.ytparty.persistence.User;
import main.java.de.voidtech.ytparty.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
		
	public boolean usernameInUse(String username) {
		return getUser(username) != null;
	}
	
	public User getUser(String username) {
		return userRepository.getUser(username);
	}
	
	public void saveUser(User user) {
		userRepository.save(user);
	}
	
	public void removeUser(String username) {
		userRepository.deleteUser(username);
	}
}