package main.java.de.voidtech.ytparty.terminal.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Command;
import main.java.de.voidtech.ytparty.terminal.AbstractCommand;

@Command
public class HelpCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;
	
	@Override
	public void run(List<String> args) {
		if (!commands.contains(this)) commands.add(this);
		StringBuilder sb = new StringBuilder();
		
		sb.append("YTParty Command Help\n");
		sb.append("Arguments surrounded with <> are optional. Arguments surrounded with [] are mandatory.\n\n");
		
		if (args.isEmpty()) {
			sb.append("All Commands:\n\n");
			for (AbstractCommand command : commands) {
				sb.append(command.getCommandName() + "\n");
			}	
		} else {
			List<AbstractCommand> commandOptions = commands.stream()
					.filter(command -> command.getCommandName().equals(args.get(0))).collect(Collectors.toList());
			if (commandOptions.isEmpty()) sb.append("Command not found");
			else {
				AbstractCommand command = commandOptions.get(0);
				sb.append(command.getUsage());
			}
		}
		
		System.out.println(sb.toString());
	}

	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public String getUsage() {
		return "help <command>";
	}

}
