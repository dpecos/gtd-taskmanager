package com.danielpecos.gtm.model.beans;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.model.persistence.Persistable;

public class Task implements Persistable {
	public enum Type {
		Normal, Web, Call_SMS, Email, Location 
	}
	public enum Status {
		Active, Completed, Discarded, Discarded_Completed 
	}
	public enum Priority {
		Low, Normal, Important, Critical
	}

	long id;
	String name;
	String description;
	Status status;
	Priority priority;
	Date dueDate;
	GeoPoint location;
	Type type;

	public Task() {
		this.priority = Priority.Normal;
		this.status = Status.Active;
		this.type = Type.Normal;
	}

	public Task(String name, String description, Priority priority) {
		this();
		this.name = name;
		this.description = description;
	}

	public long getId() {
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

	public void setDescription(String description) {
		this.description = description;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@Override
	public long store(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(GTDSQLHelper.TASK_NAME, this.name);
		values.put(GTDSQLHelper.TASK_DESCRIPTION, this.description);
		values.put(GTDSQLHelper.TASK_STATUS, this.status.toString());
		values.put(GTDSQLHelper.TASK_PRIORITY, this.priority.toString());
		return db.insert(GTDSQLHelper.TABLE_TASKS, null, values);
	}

	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		int i = 0;
		this.id = cursor.getLong(i++);
		this.name = cursor.getString(i++);
		this.description = cursor.getString(i++);
		this.status = Status.valueOf(cursor.getString(i++));
		this.priority = Priority.valueOf(cursor.getString(i++));
		return true;
	}

	@Override
	public boolean remove(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (this.id != 0) {
			return db.delete(GTDSQLHelper.TABLE_TASKS, BaseColumns._ID + "=" + this.getId(), null) > 0;
		} else {
			return false;
		}
	}

}
