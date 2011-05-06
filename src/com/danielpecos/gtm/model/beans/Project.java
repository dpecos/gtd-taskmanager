package com.danielpecos.gtm.model.beans;



public class Project extends TaskContainer {
	long id;
	String name;
	String description;
	
	public Project(String name, String description) {
		this.id = (long) (Math.random() * 10000);
		this.name = name;
		this.description = description;
	}

	public Long getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public Task elementAt(int position) {
		Task task = (Task) this.tasks.values().toArray()[position];
		return task;
	}
}
