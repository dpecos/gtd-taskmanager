package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;

public abstract class TaskContainer implements Iterable<Task> {
	HashMap<Long, Task> tasks;
	
	public TaskContainer() {
		this.tasks = new LinkedHashMap<Long, Task>();
	}
	
	public void addTask(Task task) {
		this.tasks.put(task.getId(), task);
	}
	
	public Task createTask(String name, String description, Task.Priority priority) {
		Task task = new Task(name, description, priority);
		this.addTask(task);
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
	
	protected boolean loadTasks(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.query(GTDSQLHelper.TABLE_TASKS, null, null, null, null,
				null, null);

		while (cursor.moveToNext()) {
			Task t = new Task();
			if (!t.load(cursor)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean storeTasks(GTDSQLHelper helper) {
		for (Task t : this) {
			if (t.store(helper) < 0) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean removeTasks(GTDSQLHelper helper) {
		for (Task t : this) {
			if (!t.remove(helper)) {
				return false;
			}
		}
		return true;
	}

}
