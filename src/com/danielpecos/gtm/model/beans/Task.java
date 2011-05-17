package com.danielpecos.gtm.model.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtm.model.TaskManager;
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
		this.priority = priority;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public void setName(android.content.Context ctx, String name) {
		this.name = name;
		this.store(ctx);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(android.content.Context ctx, String description) {
		this.description = description;
		this.store(ctx);
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(android.content.Context ctx, Status status) {
		this.status = status;
		this.store(ctx);
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(android.content.Context ctx, Priority priority) {
		this.priority = priority;
		this.store(ctx);
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(android.content.Context ctx, Date dueDate) {
		this.dueDate = dueDate;
		this.store(ctx);
	}

	@Override
	public long store(android.content.Context ctx) {
		GTDSQLHelper helper = new GTDSQLHelper(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();
		
		long result = 0;
		
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ContentValues values = new ContentValues();
		values.put(GTDSQLHelper.TASK_NAME, this.name);
		values.put(GTDSQLHelper.TASK_DESCRIPTION, this.description);
		values.put(GTDSQLHelper.TASK_STATUS, this.status.toString());
		values.put(GTDSQLHelper.TASK_PRIORITY, this.priority.toString());
		if (this.getDueDate() != null) {
			values.put(GTDSQLHelper.TASK_DUEDATETIME, iso8601Format.format(this.getDueDate()));
//		} else {
//			values.put(GTDSQLHelper.TASK_DUEDATETIME, null);
		}
		
		if (this.id == 0) {
			this.id = db.insert(GTDSQLHelper.TABLE_TASKS, null, values);
			result = this.id;
		} else {
			if (db.update(GTDSQLHelper.TABLE_TASKS, values, BaseColumns._ID + "=" + this.getId(), null) > 0) {
				result = this.id;
			} else {
				result = -1;
			}
		}
		
		db.close();
		
		return result;
	}

	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		int i = 0;
		this.id = cursor.getLong(i++);
		this.name = cursor.getString(i++);
		this.description = cursor.getString(i++);
		this.status = Status.valueOf(cursor.getString(i++));
		this.priority = Priority.valueOf(cursor.getString(i++));
		try {
			String date = cursor.getString(i++);
			if (date != null && !date.equalsIgnoreCase("")) {
				this.dueDate = iso8601Format.parse(date);
			}
		} catch (ParseException e) {
			Log.e(TaskManager.TAG, e.getMessage());
		}
		return true;
	}

	@Override
	public boolean remove(android.content.Context ctx, SQLiteDatabase dbParent) {

		SQLiteDatabase db = null;

		if (dbParent == null) {
			GTDSQLHelper helper = new GTDSQLHelper(ctx);
			db = helper.getWritableDatabase();
		} else {
			db = dbParent;
		}

		boolean result = false;
		if (this.id != 0) {
			result = db.delete(GTDSQLHelper.TABLE_TASKS, BaseColumns._ID + "=" + this.getId(), null) > 0;
		} else {
			result =  false;
		}
		
		if (dbParent == null) {
			db.close();
		}
		
		return result;
	}

}

