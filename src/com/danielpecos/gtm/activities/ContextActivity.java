package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter;
import com.danielpecos.gtm.views.ProjectViewHolder;

public class ContextActivity extends ExpandableListActivity {

	private TaskManager taskManager;

	private View triger_view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.context_layout);

		loadTestData();

		ArrayList<HashMap<String, String>> groupData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> childrenData_projects =  new ArrayList<ArrayList<HashMap<String, String>>>();
		ArrayList<ArrayList<HashMap<String, String>>> childrenData_tasks =  new ArrayList<ArrayList<HashMap<String, String>>>();

		Collection<Context> contexts = taskManager.getContexts();

		for (Context ctx : contexts) {
			HashMap<String, String> contextData = new HashMap<String, String>();
			contextData.put("name", ctx.getName());
			groupData.add(contextData);

			ArrayList<HashMap<String, String>> childData = new ArrayList<HashMap<String,String>>();
			for (Project project : ctx.getProjects()) {
				HashMap<String, String> projectData = new HashMap<String, String>();
				projectData.put("name", project.getName());
				projectData.put("description", project.getDescription());
				projectData.put("status_text", project.getCompletedTasksCount() + "/" + project.getTasksCount());
				projectData.put("status_icon", "" + ProjectViewHolder.getProjectStatusIcon(project.getTasksCount(), project.getCompletedTasksCount()));
				childData.add(projectData);
			}
			childrenData_projects.add(childData);
			
			childData = new ArrayList<HashMap<String,String>>();
			for (Task task : ctx.getTasks()) {
				HashMap<String, String> taskData = new HashMap<String, String>();
				taskData.put("name", task.getName());
				taskData.put("description", task.getDescription());
				taskData.put("status", "" + (task.getStatus() == Task.Status.Complete));
				childData.add(taskData);
			}
			childrenData_tasks.add(childData);
		}

		// Set up our adapter
		/*this.setListAdapter(new SimpleExpandableListAdapter(
				this, 
				groupData, 
				R.layout.context_item, 
				new String[] {"name"}, 
				new int[] {R.id.context_name}, 
				childrenData, 
				R.layout.project_item, 
				new String[] {"name", "description", "status_text"},
				new int[] {R.id.project_name, R.id.project_description, R.id.project_status_text}
		));*/
		
		this.setListAdapter(new ExpandableNestedMixedListAdapter(
				this, 
				groupData, 
				R.layout.context_item, 
				new String[] {"name"}, 
				new int[] {R.id.context_name}, 
				childrenData_projects, 
				R.layout.project_item, 
				new String[] {"name", "description", "status_text", "status_icon"},
				new int[] {R.id.project_name, R.id.project_description, R.id.project_status_text, R.id.project_status_icon},
				childrenData_tasks, 
				R.layout.task_item, 
				new String[] {"name", "description", "status"},
				new int[] {R.id.task_name, R.id.task_description, R.id.task_status}
		));
	}


	private void loadTestData() {
		// Test data
		this.taskManager = TaskManager.getInstance();
		Context ctx = this.taskManager.createContext("Contexto 1");
		Project prj = ctx.createProject("Proyecto 1.1", "Descripción de proyecto 1.1");
		prj.createTask("Tarea 1", "Tarea num 1.1.1", Task.Priority.Critical);
		prj.createTask("Tarea 2", "Tarea num 1.1.2", Task.Priority.Important).setStatus(Task.Status.Complete);
		prj.createTask("Tarea 3", "Tarea num 1.1.3", Task.Priority.Low).setStatus(Task.Status.Complete);
		prj.createTask("Tarea 4", "Tarea num 1.1.4", Task.Priority.Important);
		prj.createTask("Tarea 5", "Tarea num 1.1.5", Task.Priority.Critical);
		
		ctx.createTask("Tarea 1", "Tarea num 1.0.1", Task.Priority.Critical);
		ctx.createTask("Tarea 2", "Tarea num 1.0.2", Task.Priority.Critical).setStatus(Task.Status.Complete);
		ctx.createTask("Tarea 3", "Tarea num 1.0.3", Task.Priority.Critical);
		
		prj = ctx.createProject("Proyecto 1.2", "Descripción de proyecto 1.2");
		prj.createTask("Tarea 1", "Tarea num 1.2.1", Task.Priority.Critical);
		prj.createTask("Tarea 2", "Tarea num 1.2.2", Task.Priority.Important);
		prj.createTask("Tarea 3", "Tarea num 1.2.3", Task.Priority.Low);
		prj.createTask("Tarea 4", "Tarea num 1.2.4", Task.Priority.Important);
		prj.createTask("Tarea 5", "Tarea num 1.2.5", Task.Priority.Critical);
		
		this.taskManager.createContext("Contexto 2");
		this.taskManager.createContext("Contexto 3");

	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, view, groupPosition, childPosition, id);

		this.triger_view = view;

		Context ctx = taskManager.elementAt(groupPosition);
		Project prj = ctx.elementAt(childPosition);
		ActivityUtils.showProjectActivity(this, ctx, prj);

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ActivityUtils.PROJECT_ACTIVITY) {
			if (this.triger_view != null) {
				String context_name = (String) data.getSerializableExtra("context_name");
				Long project_id = (Long) data.getSerializableExtra("project_id");

				Project project = this.taskManager.getContext(context_name).getProject(project_id);

				ProjectViewHolder projectViewHolder = new ProjectViewHolder(this, this.triger_view);
				projectViewHolder.updateView(project);

				this.triger_view = null;
			}
		}
	}
}