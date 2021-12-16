package main.java.de.voidtech.ytparty.terminal.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Command;
import main.java.de.voidtech.ytparty.service.LogService;
import main.java.de.voidtech.ytparty.terminal.AbstractCommand;

@Command
public class LogCommand extends AbstractCommand {

	@Autowired
	private LogService logService;
	
	@Override
	public void run(List<String> args) {
		logService.sendNotification(String.join(" ", args));
	}

	@Override
	public String getCommandName() {
		return "log";
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

	@Override
	public String getUsage() {
		return "log [message]";
	}

}
