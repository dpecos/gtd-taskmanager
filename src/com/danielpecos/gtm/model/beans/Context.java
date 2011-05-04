package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.Hashtable;

public class Context extends TaskContainer {
	int id;
	String name;

	Hashtable<String, Project> projects;

	public Context(String name) {
		this.projects = new Hashtable<String, Project>();
		this.name = name;
	}

	public Project createProject(String name, String description) {
		Project project = new Project(name, description);
		this.projects.put(name, project);
		return project;
	}

	public Project getProject(String name) {
		return this.projects.get(name);
	}
	
	public Collection<Project> getProjects() {
		return this.projects.values();
	}

	public void deleteProject(Project project) {
		for (Task task : project) {
			project.deleteTask(task);
		}
		this.projects.remove(name);
	}

	public String getName() {
		return name;
	}

}
