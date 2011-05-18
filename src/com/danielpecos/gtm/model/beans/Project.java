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

	public void setName(android.content.Context ctx, String name) {
		this.name = name;
		this.store(ctx);
	}

	public String getDescription() {
		return description;
	}

	@Override
	public long store(android.content.Context ctx) {
		GTDSQLHelper helper = new GTDSQLHelper(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(GTDSQLHelper.PROJECT_NAME, this.name);
		values.put(GTDSQLHelper.PROJECT_DESCRIPTION, this.description);
		values.put(GTDSQLHelper.PROJECT_CONTEXTID, this.context_id);

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
		db.close();
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
		
		return result;
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
