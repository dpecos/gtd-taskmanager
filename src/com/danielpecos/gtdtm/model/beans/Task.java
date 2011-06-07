package com.danielpecos.gtdtm.model.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtdtm.model.persistence.Persistable;
import com.google.android.maps.GeoPoint;

public class Task implements Persistable, Cloneable {
	private static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
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
	byte[] picture;
	String googleId;
	Date lastTimePersisted;

	Task() {
		this.priority = Priority.Normal;
		this.status = Status.Active;
		this.type = Type.Normal;
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

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
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
		values.put(GTDSQLHelper.TASK_DUEDATETIME, this.getDueDate() != null ? iso8601Format.format(this.getDueDate()) : null);
		values.put(GTDSQLHelper.TASK_PICTURE, this.getPicture() != null ? this.getPicture() : null);
		values.put(GTDSQLHelper.TASK_LOCATION_LAT, this.getLocation() != null ? this.getLocation().getLatitudeE6() : null);
		values.put(GTDSQLHelper.TASK_LOCATION_LONG, this.getLocation() != null ? this.getLocation().getLongitudeE6() : null);
		values.put(GTDSQLHelper.TASK_GOOGLE_ID, this.googleId);
		values.put(GTDSQLHelper.TASK_LAST_TIME_PERSISTED, iso8601Format.format(date));
		
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
			try {
				String date = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_DUEDATETIME));
				if (date != null && !date.equalsIgnoreCase("")) {
					this.dueDate = iso8601Format.parse(date);
				} else {
					this.dueDate = null;
				}
			} catch (ParseException e) {
				Log.e(TaskManager.TAG, "DDBB: " + e.getMessage(), e);
			}
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_PICTURE))) {
			this.picture = cursor.getBlob(cursor.getColumnIndex(GTDSQLHelper.TASK_PICTURE));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LAT))) {
			this.location = new GeoPoint(cursor.getInt(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LAT)), cursor.getInt(cursor.getColumnIndex(GTDSQLHelper.TASK_LOCATION_LONG)));
		}

		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_GOOGLE_ID))) {
			this.googleId = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_GOOGLE_ID));
		}
		
		if (this.lastTimePersisted == null) {
			if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.TASK_LAST_TIME_PERSISTED))) {
				try {
					String date = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.TASK_LAST_TIME_PERSISTED));
					if (date != null && !date.equalsIgnoreCase("")) {
						this.lastTimePersisted = iso8601Format.parse(date);
					} else {
						this.lastTimePersisted = null;
					}
				} catch (ParseException e) {
					Log.e(TaskManager.TAG, "DDBB: " + e.getMessage(), e);
				}
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
				this.location + 
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

