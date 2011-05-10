package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;
import com.danielpecos.gtm.views.ProjectViewHolder;

public class ContextActivity extends ExpandableListActivity implements ExpandableListView.OnChildClickListener {

	private TaskManager taskManager;

	private View triger_view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.context_layout);

		loadTestData();

		ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
		ArrayList<ArrayList<HashMap<String, Object>>> childrenData_projects =  new ArrayList<ArrayList<HashMap<String, Object>>>();
		ArrayList<ArrayList<HashMap<String, Object>>> childrenData_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();
		ArrayList<ArrayList<HashMap<String, Object>>> childrenEvents_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();

		Collection<Context> contexts = taskManager.getContexts();

		for (Context ctx : contexts) {
			HashMap<String, Object> contextData = new HashMap<String, Object>();
			contextData.put("name", ctx.getName());
			groupData.add(contextData);

			ArrayList<HashMap<String, Object>> childData = new ArrayList<HashMap<String,Object>>();
			
			for (Project project : ctx.getProjects()) {
				HashMap<String, Object> projectData = new HashMap<String, Object>();
				projectData.put("name", project.getName());
				projectData.put("description", project.getDescription());
				projectData.put("status_text", project.getCompletedTasksCount() + "/" + project.getTasksCount());
				projectData.put("status_icon", ProjectViewHolder.getProjectStatusIcon(project.getTasksCount(), project.getCompletedTasksCount()));
				childData.add(projectData);
			}
			childrenData_projects.add(childData);
			
			childData = new ArrayList<HashMap<String,Object>>();
			ArrayList<HashMap<String, Object>> childEvents = new ArrayList<HashMap<String,Object>>();
			for (final Task task : ctx.getTasks()) {
				HashMap<String, Object> taskData = new HashMap<String, Object>();
				taskData.put("name", task.getName());
				taskData.put("description", task.getDescription());
				taskData.put("status", task.getStatus() == Task.Status.Complete);
				taskData.put("priority", task.getPriority());
				childData.add(taskData);
				
				HashMap<String, Object> taskEvents = new HashMap<String, Object>();
				taskEvents.put("status", new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
						task.setStatus(isChecked ? Task.Status.Complete : Task.Status.Pending);
						// this line is required to force the UI to update the checkbox view when using a real device
						buttonView.requestLayout();
					}
				});
				childEvents.add(taskEvents);
			}
			childrenData_tasks.add(childData);
			childrenEvents_tasks.add(childEvents);
		}

		// Set up our adapter
		/*this.setListAdapter(new SimpleExpandableListAdapter(
				this, 
				groupData, 
				R.layout.context_item, 
				new String[] {"name"}, 
				new int[] {R.id.context_name}, 
				childrenData_projects, 
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
				null, null,
				
				childrenData_tasks, 
				R.layout.task_item, 
				new String[] {"name", "description", "status"},
				new int[] {R.id.task_name, R.id.task_description, R.id.task_status}, 
				childrenEvents_tasks, 
				new RowDisplayListener() {
					@Override
					public void onRowDisplay(View rowView, HashMap<String, Object> data) {
						ActivityUtils.onTaskItemDisplay(ContextActivity.this, rowView, data);
					}
				}
		));
		this.getExpandableListView().setOnChildClickListener(this);
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
		ctx.createTask("Tarea 2", "Tarea num 1.0.2", Task.Priority.Important);
		ctx.createTask("Tarea 3", "Tarea num 1.0.3", Task.Priority.Normal).setStatus(Task.Status.Complete);
		ctx.createTask("Tarea 4", "Tarea num 1.0.4", Task.Priority.Low);
		
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
		
		if (childPosition < ctx.getProjects().size()) {
			Project prj = ctx.projectAt(childPosition);
			ActivityUtils.showProjectActivity(this, ctx, prj);
		} else {
			Task task = ctx.taskAt(childPosition - ctx.getProjects().size());
			ActivityUtils.showTaskActivity(this, task);
		}

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