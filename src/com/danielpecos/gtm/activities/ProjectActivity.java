package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.SimpleListAdapter;
import com.danielpecos.gtm.views.ProjectViewHolder;

public class ProjectActivity extends ListActivity {

	private TaskManager taskManager;

	private ProjectViewHolder projectViewHolder;
	private Project project;

	private View triger_view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_layout);

		this.taskManager = TaskManager.getInstance();

		String context_name = (String) getIntent().getSerializableExtra("context_name");
		Long project_id = (Long) getIntent().getSerializableExtra("project_id");

		this.project = this.taskManager.getContext(context_name).getProject(project_id);

		this.setTitle(project.getName());

		LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View projectItemView = mInflater.inflate(R.layout.project_item, null);
		projectViewHolder = new ProjectViewHolder(this, projectItemView);
		projectItemView.setTag(projectViewHolder);

		LinearLayout header = (LinearLayout)findViewById(R.id.header);
		header.addView(projectItemView);

		projectItemView.setMinimumHeight(projectItemView.getMeasuredHeight() + 8);
		projectViewHolder.getName().setTextSize(projectViewHolder.getName().getTextSize() + 4);
		projectViewHolder.getDescription().setTextSize(projectViewHolder.getDescription().getTextSize() + 4);

		projectViewHolder.updateView(project);

		ArrayList<HashMap<String, String>> itemsData = new ArrayList<HashMap<String,String>>();
		ArrayList<HashMap<String, Object>> itemsEvents = new ArrayList<HashMap<String,Object>>();
		for (final Task task : project.getTasks()) {
			HashMap<String, String> taskData = new HashMap<String, String>();
			taskData.put("name", task.getName());
			taskData.put("description", task.getDescription());
			taskData.put("status", "" + (task.getStatus() == Task.Status.Complete));
			itemsData.add(taskData);
			
			HashMap<String, Object> taskEvents = new HashMap<String, Object>();
			taskEvents.put("status", new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					task.setStatus(isChecked ? Task.Status.Complete : Task.Status.Pending);
					projectViewHolder.updateView(project);
				}
			});
			itemsEvents.add(taskEvents);
		}

		SimpleListAdapter adapter = new SimpleListAdapter(
				this,
				itemsData, 
				R.layout.task_item, 
				new String[] {"name", "description", "status"},
				new int[] {R.id.task_name, R.id.task_description, R.id.task_status},
				itemsEvents
		);

		this.setListAdapter(adapter);

		setResult(RESULT_OK, getIntent());
	}

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		super.onListItemClick(parent, view, position, id);

		this.triger_view = view;

		Task task = this.project.elementAt(position);
		ActivityUtils.showTaskActivity(ProjectActivity.this, task);
	}
}