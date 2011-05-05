package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.views.ProjectViewHolder;

public class ContextActivity extends ExpandableListActivity {

	private static final int PROJECT_ACTIVITY = 0;

	private TaskManager taskManager;

	private View triger_view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.context_layout);

		// Test data
		this.taskManager = TaskManager.getInstance();
		Project prj = this.taskManager.createContext("Contexto 1").createProject("Proyecto 1.1", "Descripción de proyecto 1.1");
		this.taskManager.createContext("Contexto 2");
		this.taskManager.createContext("Contexto 3");
		prj.createTask("Tarea 1", "Tarea num 1.1.1", Task.Priority.Critical);
		prj.createTask("Tarea 2", "Tarea num 1.1.2", Task.Priority.Important);
		prj.createTask("Tarea 3", "Tarea num 1.1.3", Task.Priority.Low);
		prj.createTask("Tarea 4", "Tarea num 1.1.4", Task.Priority.Important);
		prj.createTask("Tarea 5", "Tarea num 1.1.5", Task.Priority.Critical);

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
				projectData.put("status_text", project.getCompletedTasksCount() + "/" + project.getTasksCount());
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
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, view, groupPosition, childPosition, id);

		this.triger_view = view;

		Context ctx = taskManager.elementAt(groupPosition);
		Project prj = ctx.elementAt(childPosition);
		showProjectActivity(ctx, prj);

		return result;
	}

	private void showProjectActivity(Context context, Project project) {
		Intent intent = new Intent(this, ProjectActivity.class);
		intent.putExtra("context_name", context.getName());
		intent.putExtra("project_id", project.getId());
		startActivityForResult(intent, PROJECT_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PROJECT_ACTIVITY) {
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