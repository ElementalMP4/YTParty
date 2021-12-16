package main.java.de.voidtech.ytparty.terminal.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Command;
import main.java.de.voidtech.ytparty.service.FileReader;
import main.java.de.voidtech.ytparty.terminal.AbstractCommand;

@Command
public class ClearCacheCommand extends AbstractCommand {
	
	@Autowired
	private FileReader fileReader;

	@Override
	public void run(List<String> args) {
		fileReader.clearCache();
		System.out.println("File cache cleared");
	}

	@Override
	public String getCommandName() {
		return "clearcache";
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public String getUsage() {
		return "clearcache";
	}

}
