package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.util.ArrayList;
import java.util.List;

public class Queue<T> { //We use <T> to allow the type of the queue to be dynamically assigned
	private List<T> queue = new ArrayList<T>(); //Create a new list to contain the queue items
	
	public void push(T item) { //The push method adds to the bottom of the queue
		queue.add(item);
	}
	
	public T pop() { //The pop method either returns the front item or returns null.
		if (queue.isEmpty()) return null;
		else {
			T item = queue.get(0); //Get the first item and then remove it
			queue.remove(0);
			return item;
		}
	}
	
	public List<T> toList() { //The queue is already a list, so we can just return it
		return queue;
	}
	
	//We can use built in methods to clear and check the list
	public void clear() {
		queue.clear();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
