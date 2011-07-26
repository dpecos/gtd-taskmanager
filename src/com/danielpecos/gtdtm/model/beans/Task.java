package com.danielpecos.gtdtm.model.beans;

import java.io.Serializable;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtdtm.model.persistence.GoogleTaskHelper;
import com.danielpecos.gtdtm.model.persistence.Persistable;
import com.danielpecos.gtdtm.utils.DateUtils;
import com.google.android.maps.GeoPoint;

public class Task implements Persistable, Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

//	public enum Type {
//		Normal, Web, Call_SMS, Email, Location 
//	}
	public enum Status {
		Active, Completed, Discarded, Discarded_Completed 
	}
	public enum Priority {
		Low, Normal, Important, Critical
	}

	private long id;
	private String name;
	private String description;
	private Status status;
	private Priority priority;
	private Date dueDate;
	private Integer location_latitude;
	private Integer location_longitude;
//	private Type type;
	private byte[] picture;
	private String googleId;
	private Date lastTimePersisted;

	Task() {
		this.priority = Priority.Normal;
		this.status = Status.Active;
//		this.type = Type.Normal;
	}

	public Task(android.content.Context ctx, long task_id) {
		this();
		this.id = task_id;
		this.reload(ctx);
	}

	Task(String name, String description, Priority priority) {
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

	public Task setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Task setDescription(String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return this.status;
	}

	public Task setStatus(Status status) {
		this.status = status;
		return this;
	}

	public Priority getPriority() {
		return priority;
	}

	public Task setPriority(Priority priority) {
		this.priority = priority;
		return this;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public Task setDueDate(Date dueDate) {
		this.dueDate = dueDate;
		return this;
	}

	public byte[] getPicture() {
		return picture;
	}

	public Task setPicture(byte[] picture) {
		this.picture = picture;
		return this;
	}

	public GeoPoint getLocation() {
		if (this.location_latitude != null && this.location_longitude != null) {
			return new GeoPoint(this.location_latitude, this.location_longitude);
		} else {
			return null;
		}
	}

	public Task setLocation(GeoPoint location) {
		if (location != null) {
			this.location_latitude = location.getLatitudeE6();
			this.location_longitude = location.getLongitudeE6();
		} else {
			this.location_latitude = null;
			this.location_longitude = null;
		}
		return this;
	}

	public String getGoogleId() {
		return googleId;
	}

	public Task setGoogleId(String googleId) {
		this.googleId = googleId;
		return this;
	}

	public Date getLastTimePersisted() {
		return lastTimePersisted;
	}

	@Override
	public long store(android.content.Context ctx) {
		Date now = new Date(System.currentTimeMillis());
		return this.store(ctx, now);

	}

	public long store(android.content.Context ctx, Date date) {
		GTDSQLHelper helper = new GTDSQLHelper(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();

		long result = 0;

		ContentValues values = new ContentValues();
		values.put(GTDSQLHelper.TASK_NAME, this.name);
		values.put(GTDSQLHelper.TASK_DESCRIPTION, this.description);
		values.put(GTDSQLHelper.TASK_STATUS, this.status.toString());
		values.put(GTDSQLHelper.TASK_PRIORITY, this.priority.toString());
		values.put(GTDSQLHelper.TASK_DUEDATETIME, this.getDueDate() != null ? DateUtils.formatDate(this.getDueDate()) : null);
		values.put(GTDSQLHelper.TASK_PICTURE, this.getPicture() != null ? this.getPicture() : null);
		values.put(GTDSQLHelper.TASK_LOCATION_LAT, this.getLocation() != null ? this.getLocation().getLatitudeE6() : null);
		values.put(GTDSQLHelper.TASK_LOCATION_LONG, this.getLocation() != null ? this.getLocation().getLongitudeE6() : null);
		values.put(GTDSQLHelper.TASK_GOOGLE_ID, this.googleId);
		values.put(GTDSQLHelper.TASK_LAST_TIME_PERSISTED, DateUtils.formatDate(date));

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

		if (result != -1) {
			this.lastTimePersisted = date;
		}

		db.close();
		Log.d(TaskManager.TAG, "DDBB: Task successfully stored");
		return result;
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

		if (this.getGoogleId() != null) {
			TaskManager tm = TaskManager.getInstance(ctx);
			result = GoogleTaskHelper.doInGTasks((Activity)ctx, GoogleTaskHelper.GTASKS_DELETE_TASK, tm.findContextContainingTask(this), null, this);
			Log.d(TaskManager.TAG, "DDBB: Task successfully removed from GTasks");
		}

		Log.d(TaskManager.TAG, "DDBB: Task successfully removed");
		return result;
	}

	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
		this.name = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_NAME));
		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_DESCRIPTION))) {
			this.description = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_DESCRIPTION));
		}
		this.status = Status.valueOf(cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_STATUS)));
		this.priority = Priority.valueOf(cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_PRIORITY)));
		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_DUEDATETIME))) {
			String date = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_DUEDATETIME));
			if (date != null && !date.equalsIgnoreCase("")) {
				this.dueDate = DateUtils.parseDate(date);
			} else {
				this.dueDate = null;
			}
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_PICTURE))) {
			this.picture = cursor.getBlob(cursor.getColumnIndex(GTDSQLHelper.TASK_PICTURE));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LAT))) {
			this.location_latitude = cursor.getInt(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LAT));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LONG))) {
			this.location_longitude = cursor.getInt(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LONG));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_GOOGLE_ID))) {
			this.googleId = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_GOOGLE_ID));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_LAST_TIME_PERSISTED))) {
			String date = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_LAST_TIME_PERSISTED));
			if (date != null && !date.equalsIgnoreCase("")) {
				this.lastTimePersisted = DateUtils.parseDate(date);
			} else {
				this.lastTimePersisted = null;
			}
		}

		Log.d(TaskManager.TAG, "DDBB: Task successfully loaded");
		return true;
	}

	public boolean reload(android.content.Context ctx) {
		Log.d(TaskManager.TAG, "DDBB: Reloading Task from DDBB...");
		boolean result = false;

		GTDSQLHelper helper = new GTDSQLHelper(ctx);

		helper.getWritableDatabase();

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.query(GTDSQLHelper.TABLE_TASKS, null, BaseColumns._ID + "=" + this.id, null, null, null, null);
		while (cursor.moveToNext()) {
			result = this.load(db, cursor);
		}
		cursor.close();

		db.close();

		return result;
	}

	@Override
	public int hashCode() {
		return (
				this.id +
				this.name +
				this.description + 
				this.status + 
				this.priority + 
				this.dueDate +
				this.location_latitude +
				this.location_longitude +
				this.picture 
		).hashCode();
	}

	@Override 
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}

