package main.java.de.voidtech.ytparty.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.PasswordResetCase;

@Service
public class PasswordResetService {

	private List<PasswordResetCase> cases = new ArrayList<PasswordResetCase>();
	private static final int EXPIRED_CASE_CHECK_DELAY = 60000;
	

	PasswordResetService() {  
		Timer timer = new Timer();
		timer.schedule( new TimerTask() {
		    public void run() {
		    	Thread.currentThread().setName("Reset Case Timer");
		    	dropExpiredCases();
		    }
		 }, EXPIRED_CASE_CHECK_DELAY, EXPIRED_CASE_CHECK_DELAY);


	}
	
	private void dropExpiredCases() {
		for (PasswordResetCase resetCase : cases) {
			if (resetCase.expired()) cases.remove(resetCase);
		}
	}
	
	public PasswordResetCase getCaseFromResetToken(String token) { 
		PasswordResetCase resetCase = cases.stream()
				.filter(item -> item.getToken().equals(token))
				.findFirst().orElse(null);
		return resetCase;
	}
	
	public PasswordResetCase openPasswordResetCase(String user) {
		PasswordResetCase newCase = new PasswordResetCase(user);
		cases.add(newCase);
		return newCase;
	}
	
	public void closePasswordCase(PasswordResetCase resetCase) {
		cases.remove(resetCase);
	}
}
