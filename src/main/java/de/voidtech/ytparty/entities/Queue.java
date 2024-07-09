package main.java.de.voidtech.ytparty.entities;

import java.util.ArrayList;
import java.util.List;

public class Queue<T> {
	private final List<T> queue = new ArrayList<T>();
	
	public void push(T item) {
		queue.add(item);
	}
	
	public T pop() {
		if (queue.isEmpty()) return null;
		else {
			T item = queue.get(0);
			queue.remove(0);
			return item;
		}
	}
	
	public List<T> toList() {
		return queue;
	}
	

	public void clear() {
		queue.clear();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
