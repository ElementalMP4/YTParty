package main.java.de.voidtech.ytparty.entities.ephemeral;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Queue<T> extends AbstractCollection<T> {
	private List<T> queue;
	
	public T pop() {
		return this.queue.size() > 0 ? this.queue.remove(0) : null;
	}
	
	public void appendItem(T item) {
		this.queue.add(item);
	}
	
	public void clear() {
		this.queue = new ArrayList<T>();
	}
	
	public List<T> getAsList() {
		return this.queue;
	}
	
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) this.queue.iterator();
	}

	@Override
	public int size() {
		return this.queue.size();
	}
}