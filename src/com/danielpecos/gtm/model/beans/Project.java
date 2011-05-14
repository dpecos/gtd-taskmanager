package com.danielpecos.gtm.model.beans;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.model.persistence.Persistable;

public class Project extends TaskContainer implements Persistable {
	long id;
	long context_id;
	String name;
	String description;

	public Project(SQLiteDatabase db, Cursor cursor) {
		this.load(db, cursor);
		
		this.loadTasks(db, GTDSQLHelper.TABLE_PROJECTS_TASKS, GTDSQLHelper.PROJECT_ID + "=" + this.id);
	}

	public Project(long contextId, String name, String description) {
		this.context_id = contextId;
		this.name = name;
		this.description = description;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public Task elementAt(int position) {
		Task task = (Task) this.tasks.values().toArray()[position];
		return task;
	}

	@Override
	public long store(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (this.id == 0) {
			// insert
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.PROJECT_NAME, this.name);
			values.put(GTDSQLHelper.PROJECT_DESCRIPTION, this.description);
			values.put(GTDSQLHelper.PROJECT_CONTEXTID, this.context_id);
			this.id = db.insert(GTDSQLHelper.TABLE_PROJECTS, null, values);
			
			return this.id;
		} else {
			// update
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.PROJECT_NAME, this.name);
			values.put(GTDSQLHelper.PROJECT_DESCRIPTION, this.description);
			values.put(GTDSQLHelper.PROJECT_CONTEXTID, this.context_id);
			if (db.update(GTDSQLHelper.TABLE_PROJECTS, values, BaseColumns._ID + "=" + this.getId(), null) > 0) {
				return this.id;
			} else {
				return -1;
			}
		}
	}

	@Override
	public boolean remove(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		boolean result = false;
		if (this.id != 0) {
			result = db.delete(GTDSQLHelper.TABLE_PROJECTS, BaseColumns._ID + "=" + this.getId(), null) > 0;
		}
		
		if (result) {
			return this.removeTasks(helper);
		} else {
			return false;
		}
	}
	
	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		int i = 0;
		this.id = cursor.getLong(i++);
		this.name = cursor.getString(i++);
		this.description = cursor.getString(i++);
		this.context_id = cursor.getLong(i++);
		return true;
	}
}
