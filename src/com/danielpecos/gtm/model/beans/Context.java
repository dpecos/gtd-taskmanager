package com.danielpecos.gtm.model.beans;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Context extends TaskContainer {
	long id;
	String name;

	LinkedHashMap<Long, Project> projects;

	public Context(String name) {
		this.id = (long) (Math.random() * 10000);
		this.projects = new LinkedHashMap<Long, Project>();
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public long getId() {
		return id;
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
		Collection<Task> tasks = project.getTasks(); 
		while (!tasks.isEmpty()) {
			Object[] tasksArray = tasks.toArray();
			project.deleteTask((Task)tasksArray[0]);
		}
		this.projects.remove(project.getId());
	}

	public Project projectAt(int projectPosition) {
		Project prj = (Project) this.getProjects().toArray()[projectPosition];
		return prj;
	}
	
	public Task taskAt(int taskPosition) {
		Task task = (Task) this.getTasks().toArray()[taskPosition];
		return task;
	}

}
