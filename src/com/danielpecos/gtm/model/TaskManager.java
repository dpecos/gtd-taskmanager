package com.danielpecos.gtm.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.model.beans.TaskContainer;
import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.GoogleTasksClient;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;


public class TaskManager {
	public static final String TAG = "GTD-TaskManager";
	private static TaskManager instance;
	private static SharedPreferences preferences;

	HashMap<Long, Context> contexts;

	private static final String PREF = "com.danielpecos.gtm_preferences";

	public static TaskManager getInstance(android.content.Context ctx) {
		if (instance == null) {
			instance = new TaskManager(ctx);
		}
		return instance;
	}

	public static TaskManager reset(android.content.Context ctx) {
		instance = new TaskManager(ctx);
		return instance;
	}

	private TaskManager(android.content.Context ctx) {
		preferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());

		this.contexts = new LinkedHashMap<Long, Context>();
		this.loadDatabase(ctx);
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

	public boolean synchronizeGTasks(Activity activity, Context context) {

		SharedPreferences settings = activity.getSharedPreferences(PREF, Activity.MODE_WORLD_READABLE);
		String accountName = settings.getString("google_accountName", null);
		
		if (accountName == null) {
			ActivityUtils.showGoogleAccountActivity(activity, context, Boolean.FALSE);
		} else {
			String authToken = settings.getString("google_authToken", null);

			GoogleTasksClient client = new GoogleTasksClient(activity, context, authToken);

			boolean forceCreate = false;
			
			try {
				boolean elementUpdated = false;
				String contextListId = null;
				if (context.getGoogleId() != null) {
					contextListId = context.getGoogleId();
					elementUpdated = client.updateTaskList(contextListId, context.getName());
					if (!elementUpdated) {
						Log.e(TaskManager.TAG, "GTasks: Error updating context/taskList");
						forceCreate = true;
					}
				}
				if (!elementUpdated) {
					contextListId = client.createTaskList(context.getName());
					if (contextListId != null) {
						context.setGoogleId(contextListId);
						context.store(activity.getBaseContext());
					} else {
						Log.e(TaskManager.TAG, "GTasks: Error creating context/taskList");
					}
				}
				
				String previousProjectId = null;
				for (Project project : context.getProjects()) {
					elementUpdated = false;
					String projectId = null;
					if (project.getGoogleId() != null && !forceCreate) {
						projectId = project.getGoogleId();
						elementUpdated = client.updateTask(contextListId, project.getGoogleId(), project.getName(), project.getDescription(), null, null);
						if (!elementUpdated) {
							Log.e(TaskManager.TAG, "GTasks: Error updating project/task");
							forceCreate = true;
						} else {
							previousProjectId = projectId;
						}
					}
					if (!elementUpdated) {
						projectId = client.createTask(contextListId, null, previousProjectId, project.getName(), project.getDescription(), null, null);
						previousProjectId = projectId;
						if (projectId != null) {
							project.setGoogleId(projectId);
							project.store(activity.getBaseContext());
						} else {
							Log.e(TaskManager.TAG, "GTasks: Error creating project/task");
						}
					}
					
					exportTasks(activity, client, contextListId, project, null, forceCreate);
				}
				
				exportTasks(activity, client, contextListId, context, previousProjectId, forceCreate);

				return true;

			} catch (Exception e) {
				if (e instanceof HttpResponseException) {
					HttpResponse response = ((HttpResponseException) e).response;
					int statusCode = response.statusCode;
					
					if (statusCode == 400) {
						Log.e(TaskManager.TAG, "GTasks: error in request " + e.getMessage(), e);
						Toast.makeText(activity, R.string.gtasks_errorInRequest, Toast.LENGTH_SHORT);
					} else {
						Log.e(TaskManager.TAG, "GTasks: error in communication (maybe token has expired)", e);
						ActivityUtils.showGoogleAccountActivity(activity, context, Boolean.TRUE);	
					}
				} else {
					Log.e(TaskManager.TAG, "GTasks: unknown error: " + e.getMessage(), e);
				}
			}
		}

		return false;

	}

	private void exportTasks(Activity activity, GoogleTasksClient client, String contextListId, TaskContainer parent, String previousId, boolean forceCreate) throws IOException {
		String parentId = null;
		if (parent instanceof Project) {
			parentId = ((Project) parent).getGoogleId();
		}
		String previousTaskId = previousId;
		
		for (Task task : parent) {
			boolean elementUpdated = false;
			String taskId = null;
			if (task.getGoogleId() != null && !forceCreate) {
				taskId = task.getGoogleId();
				elementUpdated = client.updateTask(contextListId, task.getGoogleId(), task.getName(), task.getDescription(), task.getDueDate(), task.getStatus());
				if (!elementUpdated) {
					Log.e(TaskManager.TAG, "GTasks: Error updating task");
				} else {
					previousTaskId = taskId;
				}
			}
			if (!elementUpdated) {
				taskId = client.createTask(contextListId, parentId, previousTaskId, task.getName(), task.getDescription(), task.getDueDate(), task.getStatus());
				previousTaskId = taskId;
				if (taskId != null) {
					task.setGoogleId(taskId);
					task.store(activity.getBaseContext());
				} else {
					Log.e(TaskManager.TAG, "GTasks: Error creating task");
				}
			}
		}
	}

}
