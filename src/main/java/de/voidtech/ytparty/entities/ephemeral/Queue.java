package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.util.ArrayList;
import java.util.List;

public class Queue {

	private List<String> queue;
	
	Queue() {
		this.queue = new ArrayList<String>();
	}
	
	public String pop() {
		return this.queue.size() > 0 ? this.queue.remove(0) : null;
	}
	
	public void appendItem(String item) {
		this.queue.add(item);
	}
	
	public int getLength() {
		return this.queue.size();
	}
	
	public void clear() {
		this.queue = new ArrayList<String>();
	}
	
	public List<String> getAsList() {
		return this.queue;
	}
	
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}
}