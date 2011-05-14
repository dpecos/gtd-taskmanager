package com.danielpecos.gtm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.model.persistence.GTDSQLHelper;


public class TaskManager {
	static TaskManager instance;

	HashMap<Long, Context> contexts;

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
		this.contexts = new LinkedHashMap<Long, Context>();
		this.loadDatabase(ctx);
	}

	public Context createContext(android.content.Context ctx, String name) {
		Context context = new Context(name);
		if (context.store(GTDSQLHelper.getInstance(ctx)) > 0) {
			this.contexts.put(context.getId(), context);
			return context;
		} else { 
			return null;
		}
	}

	public void deleteContext(android.content.Context ctx, Context context) {
		if (context.remove(GTDSQLHelper.getInstance(ctx))) {
			this.contexts.remove(context.getId());
		}
	}

	private boolean loadDatabase(android.content.Context ctx) {
		GTDSQLHelper helper = GTDSQLHelper.getInstance(ctx);

		helper.getWritableDatabase();

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.query(GTDSQLHelper.TABLE_CONTEXTS, null, null, null, null, null, null);

		while (cursor.moveToNext()) {
			Context c = new Context(cursor);
			if (c.getId() < 0) {
				return false;
			} else {
				this.contexts.put(c.getId(), c);
				
				Cursor cursor_contexts_projects = db.query(GTDSQLHelper.TABLE_CONTEXTS_PROJECTS, null, GTDSQLHelper.CONTEXT_ID + "=" + c.getId(), null, null, null, null);
				while (cursor_contexts_projects.moveToNext()) {
					int project_id = cursor.getInt(0);
					
					Cursor cursor_projects = db.query(GTDSQLHelper.TABLE_PROJECTS, null, BaseColumns._ID + "=" + project_id, null, null, null, null);
					while (cursor_projects.moveToNext()) {
						Project project = new Project(cursor_projects);
						c.addProject(project);
					}
				}
			}
		}

		return true;
	}

	public Context getContext(Long id) {
		return this.contexts.get(id);
	}

	public Collection<Context> getContexts() {
		return this.contexts.values();
	}

	public Context elementAt(int contextPosition) {
		Context ctx = (Context) this.getContexts().toArray()[contextPosition];
		return ctx;
	}

}
