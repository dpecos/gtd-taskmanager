package com.danielpecos.gtdtm.model.beans;

import java.util.Arrays;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtdtm.model.persistence.Persistable;
import com.danielpecos.gtdtm.utils.DateUtils;

public class Project extends TaskContainer implements Persistable {
	private long id;
	private long context_id;
	private String name;
	private String description;
	private String googleId;
	private Date lastTimePersisted;

	Project(SQLiteDatabase db, Cursor cursor) {
		this.load(db, cursor);

		this.loadTasks(db, GTDSQLHelper.TABLE_PROJECTS_TASKS, GTDSQLHelper.PROJECT_ID + "=" + this.id);
	}

	Project(long contextId, String name, String description) {
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

	public void setDescription(String description) {
		this.description = description;
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

		ContentValues values = new ContentValues();
		values.put(GTDSQLHelper.PROJECT_NAME, this.name);
		values.put(GTDSQLHelper.PROJECT_DESCRIPTION, this.description);
		values.put(GTDSQLHelper.PROJECT_GOOGLE_ID, this.googleId);
		values.put(GTDSQLHelper.PROJECT_CONTEXTID, this.context_id);
		values.put(GTDSQLHelper.PROJECT_LAST_TIME_PERSISTED, DateUtils.formatDate(date));

		long result = 0;

		if (this.id == 0) {
			// insert
			this.id = db.insert(GTDSQLHelper.TABLE_PROJECTS, null, values);

			result = this.id;
		} else {
			// update
			if (db.update(GTDSQLHelper.TABLE_PROJECTS, values, BaseColumns._ID + "=" + this.getId(), null) > 0) {
				result = this.id;
			} else {
				result = -1;
			}
		}
		if (result != -1) {
			this.lastTimePersisted = date;
		}
		db.close();
		Log.d(TaskManager.TAG, "Project successfully stored");
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

		db.beginTransaction();
		try {
			if (this.id != 0) {
				result = db.delete(GTDSQLHelper.TABLE_PROJECTS, BaseColumns._ID + "=" + this.getId(), null) > 0;
			}

			if (result && this.removeTasks(ctx, db)) {
				db.setTransactionSuccessful();
				result = true;
			} else {
				result = false;
			}
		} finally {
			db.endTransaction();
			if (dbParent == null) {
				db.close();
			}
		}
		Log.d(TaskManager.TAG, "Project successfully removed");
		return result;
	}

	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		this.id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
		this.name = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.PROJECT_NAME));
		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.PROJECT_DESCRIPTION))) {
			this.description = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.PROJECT_DESCRIPTION));
		}
		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.PROJECT_GOOGLE_ID))) {
			this.googleId = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.PROJECT_GOOGLE_ID));
		}
		this.context_id = cursor.getLong(cursor.getColumnIndex(GTDSQLHelper.PROJECT_CONTEXTID));
		if (!cursor.isNull(cursor.getColumnIndex(GTDSQLHelper.PROJECT_LAST_TIME_PERSISTED))) {
			String date = cursor.getString(cursor.getColumnIndex(GTDSQLHelper.PROJECT_LAST_TIME_PERSISTED));
			if (date != null && !date.equalsIgnoreCase("")) {
				this.lastTimePersisted = DateUtils.parseDate(date);
			} else {
				this.lastTimePersisted = null;
			}
		}

		Log.d(TaskManager.TAG, "Project successfully loaded");
		return true;
	}

	public Task elementAt(int position) {
		Task[] tasks = this.tasks.values().toArray(new Task[]{});
		Arrays.sort(tasks, TaskContainer.taskComparator);
		return tasks[position];
	}
}
