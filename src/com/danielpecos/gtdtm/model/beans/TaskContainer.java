package com.danielpecos.gtdtm.model.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.persistence.GTDSQLHelper;

public abstract class TaskContainer implements Iterable<Task>, Serializable {
	private static final long serialVersionUID = 1L;

	protected HashMap<Long, Task> tasks;

	public static Comparator<Task> taskComparator;

	TaskContainer() {
		this.tasks = new LinkedHashMap<Long, Task>();
	}

	void addTask(Task task) {
		this.tasks.put(task.getId(), task);
	}

	public Task createTask(android.content.Context ctx, String name, String description, Task.Priority priority) {
		Task task = new Task(name, description, priority);

		GTDSQLHelper helper = new GTDSQLHelper(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();

		long id = task.store(ctx);
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

			db.close();

			if (result) {
				this.addTask(task);
				return task;
			} else {
				return null;
			}
		} else { 
			return null;
		}
	}

	public boolean deleteTask(android.content.Context ctx, Task task) {
		if (task.remove(ctx, null)) {
			this.tasks.remove(task.getId());
			return true;
		}
		return false;
	}

	public Task getTask(long id) {
		return this.tasks.get(id);
	}

	public Task getTaskByGoogleId(String googleId) {
		for (Task task : this.tasks.values()) {
			if (task.getGoogleId() != null && task.getGoogleId().equalsIgnoreCase(googleId)) {
				return task;
			}
		}
		return null;
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

	public int getDiscardedTasksCount() {
		int count = 0;
		for (Task task : this.tasks.values()) {
			if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
				count ++;
			}
		}
		return count;
	}

	@Override
	public Iterator<Task> iterator() {
		if (taskComparator == null) {
			taskComparator = new Comparator<Task>() {
				@Override
				public int compare(Task t1, Task t2) {
					
					String taskOrder = TaskManager.getPreferences().getString("settings_task_order", null);
					if (taskOrder.equalsIgnoreCase("priority")) {
						return -1 * t1.getPriority().compareTo(t2.getPriority());
					} else {
						return t1.getName().compareTo(t2.getName());
					}
				}
			};
		}
		List<Task> list = new ArrayList<Task>(this.tasks.values());
		Collections.sort(list, taskComparator);
		return list.iterator();
	}

	protected boolean loadTasks(SQLiteDatabase db, String table, String where) {
		Log.d(TaskManager.TAG, "Loading tasks...");
		Cursor cursor = null;

		try {
			cursor = db.query(table, null, where, null, null, null, null);

			while (cursor.moveToNext()) {
				long task_id = cursor.getLong(2);

				Cursor cursor_task = null;
				try {
					cursor_task = db.query(GTDSQLHelper.TABLE_TASKS, null, BaseColumns._ID + "=" + task_id, null, null, null, null);
					while (cursor_task.moveToNext()) {
						Task t = new Task();
						if (!t.load(db, cursor_task)) {
							return false;
						} else {
							this.tasks.put(t.getId(), t);
						}
					}
				} catch (SQLException e) {
				} finally {
					if (cursor_task != null && !cursor_task.isClosed()) {
						cursor_task.close();
					}
				}
			}

			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			Log.d(TaskManager.TAG, "Tasks successfully loaded");
		}
	}

	protected boolean removeTasks(android.content.Context ctx, SQLiteDatabase db) {
		Log.d(TaskManager.TAG, "Removing tasks...");
		for (Task t : this) {
			if (!t.remove(ctx, db)) {
				return false;
			}
		}
		
		boolean result = true;
		if (this.tasks.size() > 0) {

			if (this instanceof Context) {
				return (db.delete(GTDSQLHelper.TABLE_CONTEXTS_TASKS, GTDSQLHelper.CONTEXT_ID + "=" + ((Context)this).getId(), null) > 0);
			} else if (this instanceof Project) {
				return (db.delete(GTDSQLHelper.TABLE_PROJECTS_TASKS, GTDSQLHelper.PROJECT_ID + "=" + ((Project)this).getId(), null) > 0);
			}

			result = false;
		} else {
			result = true;
		}
		Log.d(TaskManager.TAG, "Tasks successfully removed");
		return result;
	}

}
