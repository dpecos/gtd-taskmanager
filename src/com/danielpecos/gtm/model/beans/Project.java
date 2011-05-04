package com.danielpecos.gtm.model.beans;


public class Project extends TaskContainer {
	int id;
	String name;
	String description;
	
	public Project(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

}
