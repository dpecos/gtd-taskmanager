package com.danielpecos.gtm.model.beans;

import java.util.Hashtable;
import java.util.Iterator;

public abstract class TaskContainer implements Iterable<Task>{
	Hashtable<Long, Task> tasks;
	
	public TaskContainer() {
		this.tasks = new Hashtable<Long, Task>();
	}
	
	public Task createTask(String name, String description, Task.Priority priority) {
		Task task = new Task(name, description, priority);
		this.tasks.put(task.getId(), task);
		return task;
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
