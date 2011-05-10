package com.danielpecos.gtm.model.beans;

import java.util.ArrayList;
import java.util.Date;

public class Task {
	public enum Type {
		Normal, Web, Call_SMS, Email, Location 
	}
	public enum Status {
		Active, Completed, Discarded, Discarded_Completed 
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
		
		this.status = Status.Active;
		this.type = Type.Normal;
		this.tags = new ArrayList<String>();
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
}
