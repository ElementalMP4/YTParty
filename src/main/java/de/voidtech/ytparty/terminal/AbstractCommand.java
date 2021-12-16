package main.java.de.voidtech.ytparty.terminal;

import java.util.List;

public abstract class AbstractCommand {

	public abstract void run(List<String> args);
	public abstract String getCommandName();
	public abstract boolean requiresArguments();
	public abstract String getUsage();
	
}
