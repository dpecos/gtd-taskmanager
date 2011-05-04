package com.danielpecos.gtm.model.beans;

import java.util.ArrayList;
import java.util.Date;

public class Task {
	public enum Type {
		Normal, Web, Call_SMS, Email, Location 
	}
	public enum Status {
		
	}
	public enum Priority {
		Low, Important, Critical
	}
	
	int id;
	String name;
	String description;
	Status status;
	Priority priority;
	Date dueDate;
	GeoPoint location;
	Type type;
	ArrayList<String> tags;
	
	public Task() {
		this.tags = new ArrayList<String>();
	}
	
	public int getId() {
		return this.id;
	}
}
