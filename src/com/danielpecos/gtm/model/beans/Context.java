package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Context extends TaskContainer {
	int id;
	String name;

	LinkedHashMap<Long, Project> projects;

	public String getName() {
		return name;
	}
	
	public Context(String name) {
		this.projects = new LinkedHashMap<Long, Project>();
		this.name = name;
	}

	public Project createProject(String name, String description) {
		Project project = new Project(name, description);
		this.projects.put(project.getId(), project);
		return project;
	}

	public Project getProject(Long id) {
		return this.projects.get(id);
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

	public Project elementAt(int projectPosition) {
		Project prj = (Project) this.getProjects().toArray()[projectPosition];
		return prj;
	}

}
