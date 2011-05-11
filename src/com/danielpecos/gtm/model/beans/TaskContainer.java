package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class TaskContainer implements Iterable<Task>{
	HashMap<Long, Task> tasks;
	
	public TaskContainer() {
		this.tasks = new LinkedHashMap<Long, Task>();
	}
	
	public Task createTask(String name, String description, Task.Priority priority) {
		Task task = new Task(name, description, priority);
		this.tasks.put(task.getId(), task);
		return task;
	}
	
	public void deleteTask(Task task) {
		this.tasks.remove(task.getId());
	}
	
	public Task getTask(long id) {
		return this.tasks.get(id);
	}
	
	public Collection<Task> getTasks() {
		return this.tasks.values();
	}
	
	public int getTasksCount() {
		return this.tasks.size();
	}
	
	public int getCompletedTasksCount() {
		int count = 0;
		for (Task task : this.tasks.values()) {
			if (task.getStatus() == Task.Status.Completed) {
				count ++;
			}
		}
		return count;
	}
	
	@Override
	public Iterator<Task> iterator() {
		return this.tasks.values().iterator();
	}

}
