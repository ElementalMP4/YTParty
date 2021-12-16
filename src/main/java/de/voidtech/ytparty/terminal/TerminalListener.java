package main.java.de.voidtech.ytparty.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.ytparty.annotations.Listener;

@Listener
public class TerminalListener {
	
	@Autowired
	private List<AbstractCommand> commands;

	TerminalListener() {
		Runnable commandRunnable = new Runnable() {
			@Override
			public void run() {
				listenForCommands();
			}
		};
		Thread commandThread = new Thread(commandRunnable);
		commandThread.setName("CMD Listener");
		commandThread.start();
	}

	protected void listenForCommands() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String input = scanner.nextLine();
			handleCommand(input);
		}
	}
	
	protected void handleCommand(String input) {
		List<String> inputParts = Arrays.asList(input.split(" "));
		String commandName = inputParts.get(0);
		inputParts = inputParts.subList(1, inputParts.size());
		
		List<AbstractCommand> commandOptions = commands.stream()
				.filter(command -> command.getCommandName().equals(commandName)).collect(Collectors.toList());
		
		if (commandOptions.isEmpty()) System.out.println("Command not found");
		else {
			AbstractCommand commandOption = commandOptions.get(0);
			commandOption.run(inputParts);			
		}
		
	}
	
}
