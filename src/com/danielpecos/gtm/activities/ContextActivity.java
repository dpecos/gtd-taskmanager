package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class ContextActivity extends ExpandableListActivity {

	TaskManager taskManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.context_layout);

		// Test data
		this.taskManager = new TaskManager();
		Project prj = this.taskManager.createContext("Contexto 1").createProject("Proyecto 1.1", "Descripción de proyecto 1.1");
		this.taskManager.createContext("Contexto 2");
		this.taskManager.createContext("Contexto 3");
		prj.createTask("Tarea 1", "Tarea num 1.1.1", Task.Priority.Important);

		ArrayList<HashMap<String, String>> groupData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> childrenData =  new ArrayList<ArrayList<HashMap<String, String>>>();
		
		Collection<Context> contexts = taskManager.getContexts();
		
		for (Context ctx : contexts) {
			HashMap<String, String> contextData = new HashMap<String, String>();
			contextData.put("name", ctx.getName());
			groupData.add(contextData);
			
			Collection<Project> projects = ctx.getProjects();

			ArrayList<HashMap<String, String>> childData = new ArrayList<HashMap<String,String>>();
			for (Project project : projects) {
				HashMap<String, String> projectData = new HashMap<String, String>();
				projectData.put("name", project.getName());
				projectData.put("description", project.getDescription());
				projectData.put("status_text", "0/0");
				childData.add(projectData);
			}
			childrenData.add(childData);
		}
		
		// Set up our adapter
		this.setListAdapter(new SimpleExpandableListAdapter(
				this, 
				groupData, 
				R.layout.context_item, 
				new String[] {"name"}, 
				new int[] {R.id.context_name}, 
				childrenData, 
				R.layout.project_item, 
				new String[] {"name", "description", "status_text"},
				new int[] {R.id.project_name, R.id.project_description, R.id.project_status_text}
			)
		);
		
	}

	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, v, groupPosition, childPosition, id);
		
		Project prj = taskManager.elementAt(groupPosition, childPosition);
		showProjectActivity(prj);
		
		return result;
	}
	
	private void showProjectActivity(Project project) {
		//Log.d(TAG, "Home: Invoco a la actividad MyMapActivity");
		
		Intent i = new Intent(this, ProjectActivity.class);    	    	
		i.putExtra("project", project);
		startActivity(i);
	}
}