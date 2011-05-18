package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.LinkedHashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.model.persistence.Persistable;

public class Context extends TaskContainer implements Persistable {
	long id;
	String name;

	LinkedHashMap<Long, Project> projects;

	private Context() {
		this.projects = new LinkedHashMap<Long, Project>();
	}

	public Context(SQLiteDatabase db, Cursor cursor) {
		this();
		this.load(db, cursor);
	}

	public Context(String name) {
		this();
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(android.content.Context ctx, String name) {
		this.name = name;
		this.store(ctx);
	}

	void addProject(Project project) {
		this.projects.put(project.getId(), project);
	}

	public Project createProject(android.content.Context ctx, String name, String description) {
		Project project = new Project(this.getId(), name, description);

		long id = project.store(ctx);
		if (id > 0) {
			this.addProject(project);
			return project;
		} else { 
			return null;
		}
	}

	public void updateProject(android.content.Context ctx, String name, Project project) {
		project.store(ctx);
		if (this.projects.remove(name) != null) {
			this.projects.put(project.getId(), project);
		}
	}

	public boolean deleteProject(android.content.Context ctx, Project project) {
		if (project.remove(ctx, null)) {
			this.projects.remove(project.getId());
			return true;
		}
		return false;
	}

	public Project getProject(Long id) {
		return this.projects.get(id);
	}

	public Collection<Project> getProjects() {
		return this.projects.values();
	}

	@Override
	public long store(android.content.Context ctx) {
		GTDSQLHelper helper = new GTDSQLHelper(ctx);
		SQLiteDatabase db = helper.getWritableDatabase();
		long result = 0;
		if (this.id == 0) {
			// insert
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.CONTEXT_NAME, this.name);
			this.id = db.insert(GTDSQLHelper.TABLE_CONTEXTS, null, values);
			result = this.id;
		} else {
			// update
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.CONTEXT_NAME, this.name);
			if (db.update(GTDSQLHelper.TABLE_CONTEXTS, values, BaseColumns._ID + "=" + this.getId(), null) > 0) {
				result = this.id;
			} else {
				result = -1;
			}
		}
		helper.close();
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
				result = db.delete(GTDSQLHelper.TABLE_CONTEXTS, BaseColumns._ID + "=" + this.getId(), null) > 0;
			}

			if (result && this.removeProjects(ctx, db) && this.removeTasks(ctx, db)) {
				db.setTransactionSuccessful();
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

	protected boolean removeProjects(android.content.Context ctx, SQLiteDatabase db) {
		for (Project p : this.getProjects()) {
			if (!p.remove(ctx, db)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean load(SQLiteDatabase db, Cursor cursor) {
		int i = 0;
		this.id = cursor.getLong(i++);
		this.name = cursor.getString(i++);

		Cursor cursor_projects = null;
		try {
			cursor_projects= db.query(GTDSQLHelper.TABLE_PROJECTS, null, GTDSQLHelper.PROJECT_CONTEXTID + "=" + this.id, null, null, null, null);
			while (cursor_projects.moveToNext()) {
				Project project = new Project(db, cursor_projects);
				this.addProject(project);
			}

			return this.loadTasks(db, GTDSQLHelper.TABLE_CONTEXTS_TASKS, GTDSQLHelper.CONTEXT_ID + "=" + this.id);
		} catch (SQLException e) {
			return false;
		} finally {
			if (cursor_projects != null && !cursor_projects.isClosed()) {
				cursor_projects.close();
			}
		}
	}

}
