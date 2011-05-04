package com.danielpecos.gtm.model.beans;

import java.util.Hashtable;
import java.util.Iterator;

public abstract class TaskContainer implements Iterable<Task>{
	Hashtable<Integer, Task> tasks;
	
	public TaskContainer() {
		this.tasks = new Hashtable<Integer, Task>();
	}
	
	public void createTask(Task task) {
		this.tasks.put(task.getId(), task);
	}
	
	public void deleteTask(Task task) {
		this.tasks.remove(task.getId());
	}
	
	public Task getTask(int id) {
		return this.tasks.get(id);
	}
	
	@Override
	public Iterator<Task> iterator() {
		return this.tasks.values().iterator();
	}
}
