package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;

public abstract class TaskContainer implements Iterable<Task> {
	HashMap<Long, Task> tasks;

	public TaskContainer() {
		this.tasks = new LinkedHashMap<Long, Task>();
	}

	public void addTask(Task task) {
		this.tasks.put(task.getId(), task);
	}

	public Task createTask(android.content.Context ctx, String name, String description, Task.Priority priority) {
		Task task = new Task(name, description, priority);

		GTDSQLHelper helper = GTDSQLHelper.getInstance(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();

		db.beginTransaction();

		try {
			long id = task.store(helper);
			if (id > 0) {
				boolean result = false;

				ContentValues values = new ContentValues();

				values.put(GTDSQLHelper.TASK_ID, id);
				if (this instanceof Context) {
					values.put(GTDSQLHelper.CONTEXT_ID, ((Context)this).getId());
					result = db.insert(GTDSQLHelper.TABLE_CONTEXTS_TASKS, null, values) > 0;
				} else if (this instanceof Project) {
					values.put(GTDSQLHelper.PROJECT_ID, ((Project)this).getId());
					result = db.insert(GTDSQLHelper.TABLE_PROJECTS_TASKS, null, values) > 0;
				}

				if (result) {
					db.setTransactionSuccessful();
					this.addTask(task);
					return task;
				} else {
					return null;
				}
			} else { 
				return null;
			}
		} finally {
			db.endTransaction();
		}
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

	protected boolean loadTasks(SQLiteDatabase db, String table, String where) {
		Cursor cursor = db.query(table, null, where, null, null, null, null);

		while (cursor.moveToNext()) {
			long task_id = cursor.getLong(2);

			Cursor cursor_task = db.query(GTDSQLHelper.TABLE_TASKS, null, BaseColumns._ID + "=" + task_id, null, null, null, null);
			while (cursor_task.moveToNext()) {
				Task t = new Task();
				if (!t.load(db, cursor_task)) {
					return false;
				} else {
					this.tasks.put(t.getId(), t);
				}
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
