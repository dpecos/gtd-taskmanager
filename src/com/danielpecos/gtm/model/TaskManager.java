package com.danielpecos.gtm.model;

import java.util.Collection;
import java.util.Hashtable;

import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class TaskManager {
	static TaskManager instance;
	
	Hashtable<String, Context> contexts;
	
	public static TaskManager getInstance() {
		if (instance == null) {
			instance = new TaskManager();
		}
		return instance;
	}
	
	private TaskManager() {
		this.contexts = new Hashtable<String, Context>();
	}
	
	public Context createContext(String name) {
		Context context = new Context(name);
		this.contexts.put(name, context);
		
		return context;
	}
	
	public Context getContext(String name) {
		return this.contexts.get(name);
	}
	
	public Collection<Context> getContexts() {
		return this.contexts.values();
	}
	
	public void deleteContext(Context context) {
		
		for (Task task : context) {
			context.deleteTask(task);
		}
		
		for (Project project : context.getProjects()) {
			context.deleteProject(project);
		}
	}

	public Context elementAt(int contextPosition) {
		Context ctx = (Context) this.getContexts().toArray()[contextPosition];
		return ctx;
	}


	
	
}
