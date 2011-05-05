package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.views.ProjectViewHolder;
import com.danielpecos.gtm.views.TaskViewHolder;

public class ProjectActivity extends ListActivity {
	
	private TaskManager taskManager;
	
	private ProjectViewHolder projectViewHolder;
	private Project project;
	
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
		
		TaskItemAdapter adapter = new TaskItemAdapter(this, R.layout.task_item, R.id.task_name, new ArrayList<Task>(project.getTasks()));
		this.setListAdapter(adapter);
		this.getListView().setTextFilterEnabled(true);
		
		setResult(RESULT_OK, getIntent());
	}
	
	private class TaskItemAdapter extends ArrayAdapter<Task> {

		public TaskItemAdapter(android.content.Context context, int resource, int textViewResourceId, List<Task> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TaskViewHolder taskViewHolder = null;
			
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.task_item, null);
				taskViewHolder = new TaskViewHolder(this.getContext(), convertView);
				convertView.setTag(taskViewHolder);
			}
			
			final Task task = this.getItem(position);
			
			taskViewHolder = (TaskViewHolder) convertView.getTag();
			taskViewHolder.getName().setText(task.getName());
			taskViewHolder.getDescription().setText(task.getDescription());
			taskViewHolder.getStatus().setChecked(task.getStatus() == Task.Status.Complete);
			
			
			taskViewHolder.getStatus().setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					task.setStatus(isChecked ? Task.Status.Complete : Task.Status.Pending);
					
					projectViewHolder.updateView(project);
				}
			
			});

			return convertView;
		}
	}
}