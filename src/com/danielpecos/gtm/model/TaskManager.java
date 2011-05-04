package com.danielpecos.gtm.model;

import java.util.Collection;
import java.util.Hashtable;

import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class TaskManager {
	Hashtable<String, Context> contexts;
	
	public TaskManager() {
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

	public Project elementAt(int contextPosition, int projectPosition) {
		Context ctx = (Context) this.getContexts().toArray()[contextPosition];
		Project prj = (Project) ctx.getProjects().toArray()[projectPosition];
		return prj;
	}
	
	
}
