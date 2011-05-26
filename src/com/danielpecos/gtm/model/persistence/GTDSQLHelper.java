package com.danielpecos.gtm.model.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielpecos.gtm.model.TaskManager;

public class GTDSQLHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "gtd_taskmanager.db";

	// Table name
	public static final String TABLE_CONTEXTS = "contexts";
	public static final String TABLE_PROJECTS = "projects";
	public static final String TABLE_TASKS = "tasks";
	public static final String TABLE_CONTEXTS_TASKS = "contexts_tasks";
	public static final String TABLE_PROJECTS_TASKS = "projects_tasks";


	// Columns
	public static final String CONTEXT_GOOGLE_ID = "googleId";
	public static final String CONTEXT_NAME = "name";

	public static final String PROJECT_NAME = "name";
	public static final String PROJECT_DESCRIPTION = "description";
	public static final String PROJECT_CONTEXTID = "context_id";

	public static final String TASK_NAME = "name";
	public static final String TASK_DESCRIPTION = "description";
	public static final String TASK_STATUS = "status";
	public static final String TASK_PRIORITY = "priority";
	public static final String TASK_DUEDATETIME = "due_datetime";
	public static final String TASK_PICTURE = "due_picture";

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
			+ CONTEXT_GOOGLE_ID + " integer "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_PROJECTS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ PROJECT_NAME + " text not null, "
			+ PROJECT_DESCRIPTION + " text, "
			+ PROJECT_CONTEXTID + " integer, "
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
			+ TASK_PICTURE + " blob "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_CONTEXTS_TASKS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ CONTEXT_ID + " integer, "
			+ TASK_ID + " integer, "
			+ "FOREIGN KEY(" + CONTEXT_ID + ") REFERENCES " + TABLE_CONTEXTS + "(id), "
			+ "FOREIGN KEY(" + TASK_ID + ") REFERENCES " + TABLE_TASKS + "(id) "
			+ ");";
			db.execSQL(sql);

			sql = "create table " + TABLE_PROJECTS_TASKS + "( " 
			+ BaseColumns._ID + " integer primary key autoincrement, "
			+ PROJECT_ID + " integer, "
			+ TASK_ID + " integer, "
			+ "FOREIGN KEY(" + PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(id), "
			+ "FOREIGN KEY(" + TASK_ID + ") REFERENCES " + TABLE_TASKS + "(id) "
			+ ");";
			db.execSQL(sql);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {

			Log.w(TaskManager.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

			/*String sql = null;
			if (oldVersion == 1) {
				sql = "alter table " + TABLE_TASKS + " add " + TASK_DUEDATETIME + " datetime ";
			} else if (oldVersion == 2) {
				sql = "";
			}

			if (sql != null) {
				db.execSQL(sql);
			}*/
		}
	}
	
}
