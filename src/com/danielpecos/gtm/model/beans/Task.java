package com.danielpecos.gtm.model.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.danielpecos.gtm.model.beans.Task.Status;

public class Task {
	public enum Type {
		Normal, Web, Call_SMS, Email, Location 
	}
	public enum Status {
		Pending, Complete, Discarded 
	}
	public enum Priority {
		Low, Normal, Important, Critical
	}
	
	long id;
	String name;
	String description;
	Status status;
	Priority priority;
	Date dueDate;
	GeoPoint location;
	Type type;
	ArrayList<String> tags;
	
	public Task(String name, String description, Priority priority) {
		this.id = (long) (Math.random() * 10000);
		this.name = name;
		this.description = description;
		this.priority = priority;
		
		this.status = Status.Pending;
		this.type = Type.Normal;
		this.tags = new ArrayList<String>();
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Priority getPriority() {
		return priority;
	}
	
}
