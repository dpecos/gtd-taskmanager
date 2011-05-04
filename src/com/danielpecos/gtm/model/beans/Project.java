package com.danielpecos.gtm.model.beans;


public class Project extends TaskContainer {
	int id;
	String name;
	
	public Project(String name) {
		this.name = name;
	}
}
