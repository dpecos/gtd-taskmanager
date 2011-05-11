package com.danielpecos.gtm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class TaskManager {
	static TaskManager instance;
	
	HashMap<Long, Context> contexts;
	
	public static TaskManager getInstance() {
		if (instance == null) {
			instance = new TaskManager();
		}
		return instance;
	}
	
	private TaskManager() {
		this.contexts = new LinkedHashMap<Long, Context>();
	}
	
	public Context createContext(String name) {
		Context context = new Context(name);
		this.contexts.put(context.getId(), context);
		
		return context;
	}
	
	public Context getContext(Long id) {
		return this.contexts.get(id);
	}
	
	public Collection<Context> getContexts() {
		return this.contexts.values();
	}
	
	public void deleteContext(Context context) {
		
		Collection<Task> tasks = context.getTasks(); 
		while (!tasks.isEmpty()) {
			Object[] tasksArray = tasks.toArray();
			context.deleteTask((Task)tasksArray[0]);
		}
		
		Collection<Project> projects = context.getProjects(); 
		while (!projects.isEmpty()) {
			Object[] projectsArray = projects.toArray();
			context.deleteProject((Project)projectsArray[0]);
		}
		
		this.contexts.remove(context.getId());
	}

	public Context elementAt(int contextPosition) {
		Context ctx = (Context) this.getContexts().toArray()[contextPosition];
		return ctx;
	}


	
	
}
