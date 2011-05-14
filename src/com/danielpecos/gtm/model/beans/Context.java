package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.LinkedHashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.model.persistence.Persistable;

public class Context extends TaskContainer implements Persistable {
	long id;
	String name;

	LinkedHashMap<Long, Project> projects;

	public Context() {
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

	public void setName(String name) {
		this.name = name;
	}

	public void addProject(Project project) {
		this.projects.put(project.getId(), project);
	}

	public Project createProject(android.content.Context ctx, String name, String description) {
		Project project = new Project(this.getId(), name, description);

		GTDSQLHelper helper = GTDSQLHelper.getInstance(ctx);
		long id = project.store(helper);
		if (id > 0) {
			this.addProject(project);
			return project;
		} else { 
			return null;
		}
	}

	public void updateProject(android.content.Context ctx, String name, Project project) {
		project.store(GTDSQLHelper.getInstance(ctx));
		if (this.projects.remove(name) != null) {
			this.projects.put(project.getId(), project);
		}
	}

	public void deleteProject(android.content.Context ctx, Project project) {
		if (project.remove(GTDSQLHelper.getInstance(ctx))) {
			this.projects.remove(project.getId());
		}
	}

	public Project getProject(Long id) {
		return this.projects.get(id);
	}

	public Collection<Project> getProjects() {
		return this.projects.values();
	}

	public Project projectAt(int projectPosition) {
		Project prj = (Project) this.getProjects().toArray()[projectPosition];
		return prj;
	}

	public Task taskAt(int taskPosition) {
		Task task = (Task) this.getTasks().toArray()[taskPosition];
		return task;
	}

	@Override
	public long store(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (this.id == 0) {
			// insert
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.CONTEXT_NAME, this.name);
			this.id = db.insert(GTDSQLHelper.TABLE_CONTEXTS, null, values);
			return this.id;
		} else {
			// update
			ContentValues values = new ContentValues();
			values.put(GTDSQLHelper.CONTEXT_NAME, this.name);
			if (db.update(GTDSQLHelper.TABLE_CONTEXTS, values, BaseColumns._ID + "=" + this.getId(), null) > 0) {
				return this.id;
			} else {
				return -1;
			}
		}
	}

	@Override
	public boolean remove(GTDSQLHelper helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		boolean result = false;
		if (this.id != 0) {
			result = db.delete(GTDSQLHelper.TABLE_CONTEXTS, BaseColumns._ID + "=" + this.getId(), null) > 0;
		}

		if (result) {
			return this.removeProjects(helper) && this.removeTasks(helper);
		} else {
			return false;
		}

	}

	protected boolean removeProjects(GTDSQLHelper helper) {
		for (Project p : this.getProjects()) {
			if (!p.remove(helper)) {
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

		Cursor cursor_projects = db.query(GTDSQLHelper.TABLE_PROJECTS, null, GTDSQLHelper.PROJECT_CONTEXTID + "=" + this.id, null, null, null, null);
		while (cursor_projects.moveToNext()) {
			Project project = new Project(db, cursor_projects);
			this.addProject(project);
		}
		
		this.loadTasks(db, GTDSQLHelper.TABLE_CONTEXTS_TASKS, GTDSQLHelper.CONTEXT_ID + "=" + this.id);

		return true;
	}

}
