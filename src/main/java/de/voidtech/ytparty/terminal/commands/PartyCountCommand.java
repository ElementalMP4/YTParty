package main.java.de.voidtech.ytparty.terminal.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Command;
import main.java.de.voidtech.ytparty.service.PartyService;
import main.java.de.voidtech.ytparty.terminal.AbstractCommand;

@Command
public class PartyCountCommand extends AbstractCommand {

	@Autowired
	private PartyService partyService;
	
	@Override
	public void run(List<String> args) {
		System.out.println("There are " + partyService.getPartyCount() + " parties currently active");
	}

	@Override
	public String getCommandName() {
		return "partycount";
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public String getUsage() {
		return "partycount";
	}
}