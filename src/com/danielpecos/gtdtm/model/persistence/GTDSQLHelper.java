package com.danielpecos.gtdtm.model.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;

public class GTDSQLHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "gtd_taskmanager.db";

	// Table name
	public static final String TABLE_CONTEXTS = "contexts";
	public static final String TABLE_PROJECTS = "projects";
	public static final String TABLE_TASKS = "tasks";
	public static final String TABLE_CONTEXTS_TASKS = "contexts_tasks";
	public static final String TABLE_PROJECTS_TASKS = "projects_tasks";

	// Columns
	public static final String CONTEXT_NAME = "name";
	public static final String CONTEXT_GOOGLE_ID = "googleId";
	public static final String CONTEXT_LAST_TIME_PERSISTED = "lastTimePersisted";

	public static final String PROJECT_NAME = "name";
	public static final String PROJECT_DESCRIPTION = "description";
	public static final String PROJECT_CONTEXTID = "context_id";
	public static final String PROJECT_GOOGLE_ID = "googleId";
	public static final String PROJECT_LAST_TIME_PERSISTED = "lastTimePersisted";

	public static final String TASK_NAME = "name";
	public static final String TASK_DESCRIPTION = "description";
	public static final String TASK_STATUS = "status";
	public static final String TASK_PRIORITY = "priority";
	public static final String TASK_DUEDATETIME = "due_datetime";
	public static final String TASK_PICTURE = "picture";
	public static final String TASK_LOCATION_LAT = "location_lat";
	public static final String TASK_LOCATION_LONG = "location_long";	
	public static final String TASK_GOOGLE_ID = "googleId";
	public static final String TASK_LAST_TIME_PERSISTED = "lastTimePersisted";

	public static final String CONTEXT_ID = "context_id";
	public static final String PROJECT_ID = "project_id";
	public static final String TASK_ID = "task_id";

	public GTDSQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.close();
	}		

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			String sql = "create table " + TABLE_CONTEXTS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, " 
			+ CONTEXT_NAME + " text not null, "
			+ CONTEXT_GOOGLE_ID + " text, "
			+ CONTEXT_LAST_TIME_PERSISTED + " datetime "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_PROJECTS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ PROJECT_NAME + " text not null, "
			+ PROJECT_DESCRIPTION + " text, "
			+ PROJECT_GOOGLE_ID + " text, "
			+ PROJECT_CONTEXTID + " integer not null, "
			+ PROJECT_LAST_TIME_PERSISTED + " datetime, "
			+ "FOREIGN KEY(" + PROJECT_CONTEXTID + ") REFERENCES " + TABLE_CONTEXTS + "(id) "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_TASKS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ TASK_NAME + " text not null, "
			+ TASK_DESCRIPTION + " text, "
			+ TASK_STATUS + " text not null, "
			+ TASK_PRIORITY + " text not null, "
			+ TASK_DUEDATETIME + " datetime, "
			+ TASK_PICTURE + " blob, "
			+ TASK_LOCATION_LAT + " integer, "
			+ TASK_LOCATION_LONG + " integer, "
			+ TASK_GOOGLE_ID + " text, "
			+ TASK_LAST_TIME_PERSISTED + " datetime "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_CONTEXTS_TASKS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ CONTEXT_ID + " integer not null, "
			+ TASK_ID + " integer not null, "
			+ "FOREIGN KEY(" + CONTEXT_ID + ") REFERENCES " + TABLE_CONTEXTS + "(id), "
			+ "FOREIGN KEY(" + TASK_ID + ") REFERENCES " + TABLE_TASKS + "(id) "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_PROJECTS_TASKS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ PROJECT_ID + " integer not null, "
			+ TASK_ID + " integer not null, "
			+ "FOREIGN KEY(" + PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(id), "
			+ "FOREIGN KEY(" + TASK_ID + ") REFERENCES " + TABLE_TASKS + "(id) "
			+ ");";
			db.execSQL(sql);

			db.setTransactionSuccessful();

			Log.i(TaskManager.TAG, "Database initialization successful");
		} finally {
			db.endTransaction();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {

			Log.w(TaskManager.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

			db.beginTransaction();
			if (oldVersion == 1 || oldVersion == 2) {
				// there was a bug here the first time the ddbb was upgrade, that's why this code is so...
				try {
					db.execSQL("alter table " + TABLE_CONTEXTS + " add " + CONTEXT_LAST_TIME_PERSISTED + " datetime; ");
				} catch (Exception e) {
				}
				try {
					db.execSQL("alter table " + TABLE_PROJECTS + " add " + PROJECT_LAST_TIME_PERSISTED + " datetime; ");
					db.execSQL("alter table " + TABLE_TASKS + " add " + TASK_LAST_TIME_PERSISTED + " datetime; ");
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();	
				}
			} else if (oldVersion == 3) {
			}

		}
	}

}
