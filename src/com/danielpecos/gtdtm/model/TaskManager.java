package com.danielpecos.gtdtm.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.beans.Project;
import com.danielpecos.gtdtm.model.beans.Task;
import com.danielpecos.gtdtm.model.persistence.GTDSQLHelper;


public class TaskManager {
	public static boolean isFullVersion(android.content.Context context) {
		return context.getString(R.string.app_version).equalsIgnoreCase("FULL");
	}

	public static final File SDCARD_DIR = new File(Environment.getExternalStorageDirectory(), "GTD-TaskManager/");
	public static final String TAG = "GTD-TaskManager";

	private static TaskManager instance;
	private static SharedPreferences preferences;

	HashMap<Long, Context> contexts;

	public static TaskManager getInstance(android.content.Context ctx) {
		if (instance == null) {
			Log.d(TAG, "TaskManager loaded");
			instance = new TaskManager(ctx);
		}
		return instance;
	}

	private TaskManager(android.content.Context ctx) {
		PreferenceManager.setDefaultValues(ctx.getApplicationContext(), R.xml.preferences, false);
		preferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());

		this.contexts = new LinkedHashMap<Long, Context>();
		this.loadDatabase(ctx);
	}

	public static TaskManager reset(android.content.Context ctx) {
		instance = new TaskManager(ctx);
		Log.d(TAG, "TaskManager reset");
		return instance;
	}

	public static SharedPreferences getPreferences() {
		return preferences;
	}

	public Context createContext(android.content.Context ctx, String name) {
		Context context = new Context(name);
		if (context.store(ctx) > 0) {
			this.contexts.put(context.getId(), context);
			return context;
		} else { 
			return null;
		}
	}

	public boolean deleteContext(android.content.Context ctx, Context context) {
		if (context.remove(ctx, null)) {
			this.contexts.remove(context.getId());
			return true;
		} else {
			return false;
		}
	}

	private boolean loadDatabase(android.content.Context ctx) {
		Log.i(TAG, "Loading data from database...");
		GTDSQLHelper helper = new GTDSQLHelper(ctx);

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = null;

		try {
			cursor = db.query(GTDSQLHelper.TABLE_CONTEXTS, null, null, null, null, null, BaseColumns._ID);

			while (cursor.moveToNext()) {
				Context c = new Context(db, cursor);
				if (c.getId() < 0) {
					Toast.makeText(ctx, R.string.error_loadingData, Toast.LENGTH_SHORT).show();
					return false;
				} else {
					this.contexts.put(c.getId(), c);
				}
			}
			return true;

		} catch (SQLException e) {
			return false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			Log.i(TAG, "Data loading finished");
		}
	}

	public boolean emptyDatabase(android.content.Context ctx) {
		GTDSQLHelper helper = new GTDSQLHelper(ctx);

		SQLiteDatabase db = helper.getWritableDatabase();

		Cursor cursor = null;

		try {
			db.delete(GTDSQLHelper.TABLE_CONTEXTS, null, null);
			db.delete(GTDSQLHelper.TABLE_PROJECTS, null, null);
			db.delete(GTDSQLHelper.TABLE_TASKS, null, null);
			db.delete(GTDSQLHelper.TABLE_CONTEXTS_TASKS, null, null);
			db.delete(GTDSQLHelper.TABLE_PROJECTS_TASKS, null, null);

			this.contexts = new HashMap<Long, Context>();
			
			return true;

		} catch (SQLException e) {
			return false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			Log.i(TAG, "Database emptied");
		}
	}

	public Context getContext(Long id) {
		return this.contexts.get(id);
	}

	public Collection<Context> getContexts() {
		return this.contexts.values();
	}

	public Context elementAt(int contextPosition) {
		Context ctx = (Context)this.getContexts().toArray()[contextPosition];
		return ctx;
	}

	public Context findContextContainingTask(Task task) {
		if (task != null) {
			for (Context context: this.getContexts()) {
				if (context.getTask(task.getId()) != null) {
					return context;
				} else {
					for (Project project : context.getProjects()) {
						if (project.getTask(task.getId()) != null) {
							return context;
						}
					}
				}
			}
		}
		return null;
	}

	public String saveToFile(android.content.Context ctx) {
		String result = null;
		String fileName = null;
		File file = null;
		
		if (Environment.getExternalStorageDirectory().canWrite()){
			
			File root = TaskManager.SDCARD_DIR;
			
			if (!root.exists()) {
				root.mkdir();
				Log.i(TaskManager.TAG, "Backup directory created");
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			fileName = sdf.format(new Date()) + ".db";
			file = new File(root, fileName);
			
			if (!file.exists() || file.canWrite()) {
				Log.d(TaskManager.TAG, "Saving contexts data to file: " + file.getAbsolutePath() + "...");

				FileOutputStream fos = null;
				ObjectOutputStream oos = null;
				try {
					fos = new FileOutputStream(file.getAbsolutePath());
					oos = new ObjectOutputStream(fos);
					oos.writeObject(this.contexts);
					oos.close();
					Log.d(TaskManager.TAG, "Data successfully saved");
					result = String.format(ctx.getString(R.string.file_saveOk), fileName);
				} catch (FileNotFoundException e) {
					Log.e(TaskManager.TAG, "File " + fileName + " could not be created", e);
				} catch (IOException e) {
					Log.e(TaskManager.TAG, "File " + fileName + " could not be written", e);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							Log.e(TaskManager.TAG, "Error closing file output stream", e);
						}
					}
					if (oos != null) {
						try {
							oos.close();
						} catch (IOException e) {
							Log.e(TaskManager.TAG, "Error closing object output stream", e);
						}
					}
				}
			} else {
				result = ctx.getString(R.string.error_file_writing);
				Log.e(TaskManager.TAG, result);
			}
		} else {
			Log.w(TaskManager.TAG, "Impossible to write to SD card");
			result = ctx.getString(R.string.error_sdcard);
		}
		return result;
	}

}
