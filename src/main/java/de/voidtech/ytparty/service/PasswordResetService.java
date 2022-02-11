package main.java.de.voidtech.ytparty.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;

import main.java.de.voidtech.ytparty.entities.ephemeral.PasswordResetCase;

@Service
public class PasswordResetService {

	private List<PasswordResetCase> cases = new ArrayList<PasswordResetCase>(); //Store a list of cases
	private static final int EXPIRED_CASE_CHECK_DELAY = 60000; //Wait 60 seconds between checking cases
	
	//This method is the constructor for the service. When the service class is instantiated, this method will run automatically.
	PasswordResetService() {  
		Timer timer = new Timer(); //Create a new timer
		timer.schedule( new TimerTask() { //Use the new timer to schedule a task
		    public void run() {
		    	Thread.currentThread().setName("Reset Case Timer"); //Set the timer thread name so we can identify it later
		    	dropExpiredCases(); //Drop expired cases every 60 seconds
		    }
		 }, EXPIRED_CASE_CHECK_DELAY, EXPIRED_CASE_CHECK_DELAY);
		//The first value is a TikerTask, the second is the period between running the task,
		//the last is the delay between the creation of the task and the first time it gets executed.
	}
	
	private void dropExpiredCases() {
		for (PasswordResetCase resetCase : cases) { //Iterate through every case
			if (resetCase.expired()) cases.remove(resetCase); //If the case has expired, delete it
		}
	}
	
	public PasswordResetCase getCaseFromResetToken(String token) { 
		PasswordResetCase resetCase = cases.stream() //Create a stream of the case list
				.filter(item -> item.getToken().equals(token)) //Filter the list for the case we are looking for
				.findFirst().orElse(null); //If we find it, return it. If not, return null
		return resetCase;
	}
	
	public PasswordResetCase openPasswordResetCase(String user) {
		PasswordResetCase newCase = new PasswordResetCase(user); //Create a new reset case for the provided username
		cases.add(newCase); //Add the case to the cases list
		return newCase; //Return this case to the original call point
	}
	
	public void closePasswordCase(PasswordResetCase resetCase) {
		cases.remove(resetCase); //Remove the case object from the cases list 
	}
}
